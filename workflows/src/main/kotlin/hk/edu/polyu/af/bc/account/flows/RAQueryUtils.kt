package hk.edu.polyu.af.bc.account.flows

import hk.edu.polyu.af.bc.account.flows.services.PersistentRegisteredAccountService
import hk.edu.polyu.af.bc.account.schemas.PersistentRA
import hk.edu.polyu.af.bc.account.states.RegisteredAccount
import net.corda.core.flows.FlowLogic
import net.corda.core.identity.Party
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.QueryCriteria.VaultQueryCriteria
import net.corda.core.node.services.vault.builder
import java.util.*

fun FlowLogic<*>.raService(): hk.edu.polyu.af.bc.account.flows.services.RegisteredAccountService =
    serviceHub.cordaService(type = PersistentRegisteredAccountService::class.java)

fun raBaseCriteria() = VaultQueryCriteria(
    status = Vault.StateStatus.UNCONSUMED,
    contractStateTypes = setOf(RegisteredAccount::class.java)
)

fun raByHostCriteria(host: Party): QueryCriteria {
    return builder {
        val partySelector = PersistentRA::host.equal(host)
        QueryCriteria.VaultCustomQueryCriteria(partySelector)
    }
}

fun raByRegistryCriteria(registry: Party): QueryCriteria {
    return builder {
        val partySelector = PersistentRA::registry.equal(registry)
        QueryCriteria.VaultCustomQueryCriteria(partySelector)
    }
}

fun raByNameCriteria(name: String): QueryCriteria {
    return builder {
        val nameSelector = PersistentRA::acctName.equal(name)
        QueryCriteria.VaultCustomQueryCriteria(nameSelector)
    }
}

fun raByTypeCriteria(type: String): QueryCriteria {
    return builder {
        val nameSelector = PersistentRA::accountType.equal(type)
        QueryCriteria.VaultCustomQueryCriteria(nameSelector)
    }
}

fun raByRAUUIDCriteria(raUUID: UUID): QueryCriteria {
    return builder {
        val uuidSelector = PersistentRA::raUUID.equal(raUUID)
        QueryCriteria.VaultCustomQueryCriteria(uuidSelector)
    }
}

fun raByAIUUIDCriteria(aiUUID: UUID): QueryCriteria {
    return builder {
        val uuidSelector = PersistentRA::aiUUID.equal(aiUUID)
        QueryCriteria.VaultCustomQueryCriteria(uuidSelector)
    }
}

