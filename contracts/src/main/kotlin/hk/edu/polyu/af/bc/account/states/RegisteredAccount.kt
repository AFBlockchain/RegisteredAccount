package hk.edu.polyu.af.bc.account.states

import com.r3.corda.lib.accounts.contracts.states.AccountInfo
import hk.edu.polyu.af.bc.account.contracts.RegisteredAccountContract
import hk.edu.polyu.af.bc.account.schemas.PersistentRA
import hk.edu.polyu.af.bc.account.schemas.RASchemaV1
import net.corda.core.contracts.*
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.node.ServiceHub
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.LedgerTransaction

@BelongsToContract(RegisteredAccountContract::class)
data class RegisteredAccount(
    val aiPtr: LinearPointer<AccountInfo>,
    val acctName: String,
    val acctDetails: AccountDetails,
    val host: Party,
    val registry: Party = host,
    val id: UniqueIdentifier = UniqueIdentifier(),
): ContractState, LinearState, QueryableState {
    override val participants: List<AbstractParty>
        get() = setOf(host, registry).toList() // make sure no duplicates

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        if (schema is RASchemaV1) {
            return PersistentRA(
                id.id,
                aiPtr.pointer.id,
                acctName,
                acctDetails.getType().repr(),
                host,
                registry
            )
        } else {
            throw NotImplementedError()
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> {
        return listOf(RASchemaV1())
    }

    override val linearId: UniqueIdentifier
        get() = id

    fun resolveAccountInfo(serviceHub: ServiceHub): StateAndRef<AccountInfo> = aiPtr.resolve(serviceHub)
    fun resolveAccountInfo(tx: LedgerTransaction): StateAndRef<AccountInfo> = aiPtr.resolve(tx)
}

@CordaSerializable
interface AccountType {
    /**
     * A String representation of this type. For uniqueness, this can simply be the fully qualified class name.
     */
    fun repr(): String
}

@CordaSerializable
abstract class AccountDetails {
    /**
     * An indicator to let implementors of subclasses know how to cast to the concrete implementations in order to read detailed
     * information contained. Note however that the cast is not safe: two different [AccountDetails] *may* yield the same [AccountType].
     * The contract here is that [AccountDetails] and [AccountType] should have strict one-to-one mapping.
     */
    abstract fun getType(): AccountType
}