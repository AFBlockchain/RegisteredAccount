package hk.edu.polyu.af.bc.account.flows.workflows

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.accounts.contracts.states.AccountInfo
import com.r3.corda.lib.accounts.workflows.flows.CreateAccount
import hk.edu.polyu.af.bc.account.contracts.RegisteredAccountContract
import hk.edu.polyu.af.bc.account.flows.defaultNotary
import hk.edu.polyu.af.bc.account.flows.services.RegisteredAccountValidationService
import hk.edu.polyu.af.bc.account.flows.services.raService
import hk.edu.polyu.af.bc.account.states.AccountDetails
import hk.edu.polyu.af.bc.account.states.RegisteredAccount
import net.corda.core.contracts.LinearPointer
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.util.*


@StartableByRPC
@StartableByService
class RegisterAccount(
    private val accountInfoUid: UniqueIdentifier,
    private val name: String,
    private val acctDetails: AccountDetails,
    private val registry: Party? = null // null indicates we are the registry
): FlowLogic<StateAndRef<RegisteredAccount>>() {
    @Suspendable
    override fun call(): StateAndRef<RegisteredAccount> {
        val account = RegisteredAccount(
            LinearPointer(accountInfoUid, AccountInfo::class.java),
            name, acctDetails, ourIdentity, registry ?: ourIdentity
        )
        return subFlow(RegisterAccountBase(account))
    }
}

/**
 * Create an [AccountInfo] at current node. And register it at the given registry with additional information
 */
@StartableByRPC
@StartableByService
class CreateAndRegisterAccount(
    private val raName: String,
    private val aiName: String = raName,
    private val details: AccountDetails,
    private val registry: Party? = null
): FlowLogic<StateAndRef<RegisteredAccount>>() {
    @Suspendable
    override fun call(): StateAndRef<RegisteredAccount> {
        val infoSaf = subFlow(CreateAccount(aiName))
        return subFlow(RegisterAccount(infoSaf.state.data.linearId, raName, details, registry))
    }
}


/**
 * Register the given [RegisteredAccount], whose properties are already populated. This flow can only be invoked by the to-be-registered
 * account's host.
 */
@InitiatingFlow
class RegisterAccountBase(
    private val acct: RegisteredAccount
): FlowLogic<StateAndRef<RegisteredAccount>>() {
    @Suspendable
    override fun call(): StateAndRef<RegisteredAccount> {
        check(acct.host == ourIdentity) {
            "Only host of the declared account can carry out registration. Expected: ${acct.host}, Found: $ourIdentity"
        }

        dbIntegrityCheck(acct)

        val txb = TransactionBuilder(defaultNotary())
        RegisteredAccountContract.generateRegister(txb, acct)
        var signed = serviceHub.signInitialTransaction(txb, acct.host.owningKey) // host signs whatever

        var registrySession: FlowSession? = null // null indicates the registry is ourselves

        // if we are the registry, directly carry out registry policy check
        if (ourIdentity == acct.registry) {
            validateFurther(acct)
        } else { // if we are not registry, send it to registry
            registrySession = initiateFlow(acct.registry)
            subFlow(CollectSignatureFlow(signed, registrySession, acct.registry.owningKey))[0].apply {
                signed = signed.withAdditionalSignature(this)
            }
        }

        val sessions = if (registrySession == null) listOf() else listOf(registrySession)
        return subFlow(FinalityFlow(signed, sessions)).coreTransaction.outRefsOfType(RegisteredAccount::class.java)[0]
    }
}

/**
 * The only case where this flow is invoked is when the node is a registry and not the host. In this case, the flow should handle signing
 * and receive final transaction.
 */
@InitiatedBy(RegisterAccountBase::class)
class RegisterAccountBaseResponder(val session: FlowSession): FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        subFlow(object: SignTransactionFlow (session) {
            override fun checkTransaction(stx: SignedTransaction) {
                val outs = stx.coreTransaction.outRefsOfType(RegisteredAccount::class.java)
                val acct: RegisteredAccount
                when (outs.size) {
                    1 -> acct = outs[0].state.data
                    else -> throw FlowException("Found ${outs.size} RegisteredAccount's in this transaction when only 1 is allowed")
                }

                dbIntegrityCheck(acct)
                validateFurther(acct)
            }
        })

        return subFlow(ReceiveFinalityFlow(session))
    }
}

fun FlowLogic<*>.dbIntegrityCheck(ra: RegisteredAccount) {
    // validate unique constraints: raUUID, aiUUID, registry + acctName
    if (raService().registeredAccountByRAUUID(ra.linearId.id) != null)
        throw FlowException("There's already an account registered with RA-UUID: ${ra.linearId.id}")

    if (raService().registeredAccountByAIUUID(ra.aiPtr.pointer.id) != null)
        throw FlowException("There's already an account registered with AI-UUID: ${ra.aiPtr.pointer.id}")

    val ret = raService().registeredAccount(ra.acctName).filter { it.state.data.registry == ra.registry }
    if (ret.isNotEmpty()) throw FlowException("There's already an account registered on registry ${ra.registry} " +
            "with name ${ra.acctName}") 
}

/**
 * Custom validation
 */
@Throws(FlowException::class)
fun FlowLogic<*>.validateFurther(ra: RegisteredAccount) {
    val loader = ServiceLoader.load(RegisteredAccountValidationService::class.java)

    if (!loader.iterator().hasNext()) {
        logger.warn("No implementation for ${RegisteredAccountValidationService::class.java.simpleName} is loaded.")
    }

    loader.forEach {
        it.validate(ra)
    }
}