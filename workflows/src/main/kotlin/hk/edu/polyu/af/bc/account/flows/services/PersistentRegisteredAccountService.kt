package hk.edu.polyu.af.bc.account.flows.services

import com.r3.corda.lib.accounts.workflows.internal.accountService
import com.r3.corda.lib.accounts.workflows.ourIdentity
import hk.edu.polyu.af.bc.account.flows.*
import hk.edu.polyu.af.bc.account.states.RegisteredAccount
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.FlowLogic
import net.corda.core.identity.Party
import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.node.services.queryBy
import net.corda.core.serialization.SingletonSerializeAsToken
import net.corda.core.utilities.contextLogger
import java.security.PublicKey
import java.util.*

@CordaService
class PersistentRegisteredAccountService(private val svc: AppServiceHub): RegisteredAccountService, SingletonSerializeAsToken() {
    companion object {
        val logger = contextLogger()
    }

    override fun allAccounts(): List<StateAndRef<RegisteredAccount>> {
        return svc.vaultService.queryBy<RegisteredAccount>(raBaseCriteria()).states
    }

    override fun accountsByHost(host: Party): List<StateAndRef<RegisteredAccount>> {
        return svc.vaultService.queryBy<RegisteredAccount>(raBaseCriteria().and(raByHostCriteria(host))).states
    }

    override fun accountsByRegistry(registry: Party): List<StateAndRef<RegisteredAccount>> {
        return svc.vaultService.queryBy<RegisteredAccount>(raBaseCriteria().and(raByRegistryCriteria(registry))).states
    }

    override fun accountsByType(typeRepr: String): List<StateAndRef<RegisteredAccount>> {
        return svc.vaultService.queryBy<RegisteredAccount>(raBaseCriteria().and(raByTypeCriteria(typeRepr))).states
    }

    override fun ourAccountsAsHost(): List<StateAndRef<RegisteredAccount>> {
        return accountsByHost(svc.ourIdentity)
    }

    override fun ourAccountsAsRegistry(): List<StateAndRef<RegisteredAccount>> {
        return accountsByRegistry(svc.ourIdentity)
    }

    override fun registeredAccount(acctName: String): List<StateAndRef<RegisteredAccount>> {
        return svc.vaultService.queryBy<RegisteredAccount>(raBaseCriteria().and(raByNameCriteria(acctName))).states
    }

    override fun registeredAccountByRAUUID(uuid: UUID): StateAndRef<RegisteredAccount>? {
        val ret = svc.vaultService.queryBy<RegisteredAccount>(raBaseCriteria().and(raByRAUUIDCriteria(uuid))).states
        return when (ret.size) {
            0 -> null
            1 -> ret[0]
            else -> {
                logger.warn("Found ${ret.size} RegisteredAccount with ra-uuid $uuid")
                throw IllegalStateException("Found ${ret.size} accounts with ra-uuid $uuid")
            }
        }
    }

    override fun registeredAccountByAIUUID(uuid: UUID): StateAndRef<RegisteredAccount>? {
        val ret = svc.vaultService.queryBy<RegisteredAccount>(raBaseCriteria().and(raByAIUUIDCriteria(uuid))).states
        return when (ret.size) {
            0 -> null
            1 -> ret[0]
            else -> { // impossible, guaranteed by database constraint
                throw IllegalStateException()
            }
        }
    }

    override fun registeredAccount(owningKey: PublicKey): StateAndRef<RegisteredAccount>? {
        val aiUUID: UUID = svc.accountService.accountIdForKey(owningKey) ?: return null // no AccountInfo found for this key

        val ret = svc.vaultService.queryBy<RegisteredAccount>(raBaseCriteria().and(raByAIUUIDCriteria(aiUUID))).states
        return when (ret.size) {
            0 -> null // no RegisteredAccount found for this AccountInfo
            1 -> ret[0]
            else -> { // Impossible, as unique constraint in the database ensures that one aiUUID maps to only one raUUID
                throw IllegalStateException()
            }
        }
    }

    override fun accountKeys(raUUID: UUID): List<PublicKey> {
        val aiUUID = registeredAccountByRAUUID(raUUID)?.state?.data?.aiPtr?.pointer?.id
        return if (aiUUID == null) listOf() else svc.accountService.accountKeys(aiUUID) // empty list means two things: no such account
    // or no keys are allocated to this account
    }
}

fun FlowLogic<*>.raService() = serviceHub.cordaService(PersistentRegisteredAccountService::class.java)