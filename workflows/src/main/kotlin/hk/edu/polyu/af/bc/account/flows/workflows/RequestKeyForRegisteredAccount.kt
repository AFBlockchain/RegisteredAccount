package hk.edu.polyu.af.bc.account.flows.workflows

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.accounts.workflows.flows.RequestKeyForAccount
import hk.edu.polyu.af.bc.account.flows.services.raService
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.AnonymousParty
import java.lang.IllegalArgumentException
import java.util.*

@StartableByRPC
class RequestKeyForRegisteredAccountByUUID(
    private val raUUID: UUID
): FlowLogic<AnonymousParty>() {
    @Suspendable
    override fun call(): AnonymousParty {
        val acct = raService().registeredAccountByRAUUID(raUUID) ?: throw IllegalArgumentException("Account with uuid $raUUID is not found")
        return subFlow(RequestKeyForAccount(acct.state.data.aiPtr.resolve(serviceHub).state.data))
    }
}

@StartableByRPC
class RequestKeyForRegisteredAccountByName(
    private val acctName: String
): FlowLogic<AnonymousParty>() {
    @Suspendable
    override fun call(): AnonymousParty {
        val ret = raService().registeredAccount(acctName).let {all -> all.filter { it.state.data.registry == ourIdentity } }
        if (ret.isEmpty()) throw IllegalArgumentException("Account with name $acctName registered with this node is not found")

        return subFlow(RequestKeyForRegisteredAccountByUUID(raService().registeredAccount(acctName)[0].state.data.id.id))
    }
}