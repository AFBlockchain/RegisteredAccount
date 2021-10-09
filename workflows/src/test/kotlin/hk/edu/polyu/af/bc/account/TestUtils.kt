package hk.edu.polyu.af.bc.account

import com.r3.corda.lib.accounts.contracts.states.AccountInfo
import com.r3.corda.lib.accounts.workflows.flows.CreateAccount
import com.r3.corda.lib.accounts.workflows.flows.RequestKeyForAccount
import hk.edu.polyu.af.bc.account.flows.services.PersistentRegisteredAccountService
import hk.edu.polyu.af.bc.account.flows.workflows.CreateAndRegisterAccount
import hk.edu.polyu.af.bc.account.states.Gender
import hk.edu.polyu.af.bc.account.states.RegisteredAccount
import hk.edu.polyu.af.bc.account.states.StandardDetails
import net.corda.cliutils.printError
import net.corda.core.concurrent.CordaFuture
import net.corda.core.contracts.LinearPointer
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import net.corda.core.utilities.getOrThrow
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.StartedMockNode
import java.security.PublicKey
import java.util.*
import kotlin.test.assertNotNull

fun randomString() = UUID.randomUUID().toString()

fun standardDetails() = StandardDetails(randomString(), randomString(), Gender.FEMALE)

fun StateAndRef<AccountInfo>.registeredAccount(registry: Party = state.data.host,
                                               name: String = randomString(),
                                               uid: UniqueIdentifier = UniqueIdentifier()) =
    RegisteredAccount(LinearPointer(state.data.identifier, AccountInfo::class.java), name, standardDetails(), state.data.host, registry, uid)


/**
 * Create an [AccountInfo] at the given node with a random name.
 */
fun StartedMockNode.accountInfo(network: MockNetwork) = startFlow(CreateAccount(randomString())).runAndGet(network)


fun <V> CordaFuture<V>.runAndGet(network: MockNetwork): V {
    network.runNetwork()
    return getOrThrow()
}

fun StartedMockNode.identity() = info.legalIdentities[0]

fun StartedMockNode.ra() = services.cordaService(PersistentRegisteredAccountService::class.java)

fun assertHaveAccount(accountUid: UniqueIdentifier, vararg nodes: StartedMockNode) {
    nodes.forEach { node ->
        assertNotNull(node.services.vaultService.queryBy(RegisteredAccount::class.java).states.find { accountUid == it.state.data.id })
    }
}

fun assertHaveAccount(ra: RegisteredAccount, vararg nodes: StartedMockNode) {
    assertHaveAccount(ra.id, *nodes)
}

fun assertHaveAccount(name: String, vararg nodes: StartedMockNode) {
    nodes.forEach { node ->
        assertNotNull(node.services.vaultService.queryBy(RegisteredAccount::class.java).states.find { name == it.state.data.acctName })
    }
}

fun StateAndRef<RegisteredAccount>.assertEquals(other: StateAndRef<RegisteredAccount>?) {
    assertNotNull(other)
    kotlin.test.assertEquals(this.state.data.id, other!!.state.data.id)
}


fun List<StateAndRef<RegisteredAccount>>.assertEquals(other: List<StateAndRef<RegisteredAccount>>) {
    printError("Comparing...")
    this.sortedBy { it.state.data.acctName }
    other.sortedBy { it.state.data.acctName }

    kotlin.test.assertEquals(this.map { it.state.data.id }, other.map { it.state.data.id })
}

fun StartedMockNode.createAndRegisterAccount(network: MockNetwork, registry: Party = this.identity(), raName: String = randomString()) =
    startFlow(CreateAndRegisterAccount(raName, randomString(), standardDetails(), registry)).runAndGet(network)

fun StartedMockNode.requestKey(ra: RegisteredAccount, network: MockNetwork): PublicKey {
    val accountInfo = ra.resolveAccountInfo(services).state.data
    return startFlow(RequestKeyForAccount(accountInfo)).runAndGet(network).owningKey
}