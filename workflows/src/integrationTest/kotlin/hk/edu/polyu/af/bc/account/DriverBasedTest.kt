package hk.edu.polyu.af.bc.account

import hk.edu.polyu.af.bc.account.flows.workflows.CreateAndRegisterAccount
import hk.edu.polyu.af.bc.account.states.Gender
import hk.edu.polyu.af.bc.account.states.StandardDetails
import net.corda.core.identity.CordaX500Name
import net.corda.testing.common.internal.testNetworkParameters
import net.corda.testing.core.TestIdentity
import net.corda.testing.driver.DriverParameters
import net.corda.testing.driver.NodeHandle
import net.corda.testing.driver.NodeParameters
import net.corda.testing.driver.driver
import net.corda.testing.node.TestCordapp
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals


class DriverBasedTest {
    private val a = TestIdentity(CordaX500Name("PartyA", "", "GB"))
    private val b = TestIdentity(CordaX500Name("PartyB", "", "US"))

    private lateinit var apps: List<TestCordapp>
    private val networkParameters = testNetworkParameters(minimumPlatformVersion = 4)

    @Before
    fun setup() {
        apps = listOf(
                TestCordapp.findCordapp("hk.edu.polyu.af.bc.account.flows"),
                TestCordapp.findCordapp("hk.edu.polyu.af.bc.account.contracts"),
                TestCordapp.findCordapp("com.r3.corda.lib.accounts.workflows"),
                TestCordapp.findCordapp("com.r3.corda.lib.accounts.contracts")
        )
    }

    @Test
    fun `crud operation on accounts`() {
        driver(DriverParameters().withNetworkParameters(networkParameters).withStartNodesInProcess(true).withCordappsForAllNodes(apps)) {
            val handleFutures = listOf(
                    startNode(NodeParameters().withProvidedName(a.name)),
                    startNode(NodeParameters().withProvidedName(b.name))
            )

            val ap = handleFutures[0].get().rpc
            val bp = handleFutures[1].get().rpc

            try {
                val future = ap.startFlowDynamic(CreateAndRegisterAccount::class.java,"a", "a", StandardDetails("","",Gender.MALE)).returnValue
                assertEquals("a", future.get().state.data.acctName)
            } catch (e: Exception) {
                throw RuntimeException("Caught exception during test: ", e)
            }
        }
    }
}