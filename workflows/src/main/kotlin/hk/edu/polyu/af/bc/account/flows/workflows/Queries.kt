package hk.edu.polyu.af.bc.account.flows.workflows

import co.paralleluniverse.fibers.Suspendable
import hk.edu.polyu.af.bc.account.flows.services.raService
import hk.edu.polyu.af.bc.account.states.RegisteredAccount
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import java.security.PublicKey
import java.util.*

@StartableByRPC
class AllRegisteredAccounts: FlowLogic<List<StateAndRef<RegisteredAccount>>>() {
    @Suspendable
    override fun call(): List<StateAndRef<RegisteredAccount>> = raService().allAccounts()
}

@StartableByRPC
class OurRegisteredAccountsAsHost: FlowLogic<List<StateAndRef<RegisteredAccount>>>() {
    @Suspendable
    override fun call(): List<StateAndRef<RegisteredAccount>> = raService().ourAccountsAsHost()
}

@StartableByRPC
class OurRegisteredAccountsAsRegistry: FlowLogic<List<StateAndRef<RegisteredAccount>>>() {
    @Suspendable
    override fun call(): List<StateAndRef<RegisteredAccount>> = raService().ourAccountsAsRegistry()
}

@StartableByRPC
class RegisteredAccountsByType(private val type: String): FlowLogic<List<StateAndRef<RegisteredAccount>>>() {
    @Suspendable
    override fun call(): List<StateAndRef<RegisteredAccount>> = raService().accountsByType(type)
}

@StartableByRPC
class RegisteredAccountsByHost(private val host: Party): FlowLogic<List<StateAndRef<RegisteredAccount>>>() {
    @Suspendable
    override fun call(): List<StateAndRef<RegisteredAccount>> = raService().accountsByHost(host)
}

@StartableByRPC
class RegisteredAccountsByRegistry(private val registry: Party): FlowLogic<List<StateAndRef<RegisteredAccount>>>() {
    @Suspendable
    override fun call(): List<StateAndRef<RegisteredAccount>> = raService().accountsByRegistry(registry)
}

@StartableByRPC
class RegisteredAccountsByName(private val name: String): FlowLogic<List<StateAndRef<RegisteredAccount>>>() {
    @Suspendable
    override fun call() = raService().registeredAccount(name)
}

@StartableByRPC
class RegisteredAccountByRAUUID(private val uuid: UUID): FlowLogic<StateAndRef<RegisteredAccount>?>() {
    @Suspendable
    override fun call() = raService().registeredAccountByRAUUID(uuid)
}

@StartableByRPC
class RegisteredAccountByAIUUID(private val uuid: UUID): FlowLogic<StateAndRef<RegisteredAccount>?>() {
    @Suspendable
    override fun call() = raService().registeredAccountByAIUUID(uuid)
}

@StartableByRPC
class RegisteredAccountForKey(private val key: PublicKey): FlowLogic<StateAndRef<RegisteredAccount>?>() {
    @Suspendable
    override fun call() = raService().registeredAccount(key)
}

@StartableByRPC
class KeysForRegisteredAccount(private val uuid: UUID): FlowLogic<List<PublicKey>>() {
    @Suspendable
    override fun call() = raService().accountKeys(uuid)
}