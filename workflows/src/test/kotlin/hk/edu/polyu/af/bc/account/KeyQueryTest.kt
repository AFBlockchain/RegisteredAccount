package hk.edu.polyu.af.bc.account

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class KeyQueryTest: TestBase() {
    @Test
    fun `can query account by key at the same node`() {
        val acct = a.createAndRegisterAccount(network)
        val key = a.requestKey(acct.state.data, network)

        acct.run {
            assertEquals(a.ra().registeredAccount(key))
        }
    }

    @Test
    fun `can query by key at different nodes`() {
        val accountAB = a.createAndRegisterAccount(network, registry = b.identity())
        val key = b.requestKey(accountAB.state.data, network)

        accountAB.run {
            assertEquals(a.ra().registeredAccount(key))
            assertEquals(b.ra().registeredAccount(key))
        }
    }

    @Test
    fun `should return all known keys for the account`() {
        val acct = a.createAndRegisterAccount(network)
        val key1 = a.requestKey(acct.state.data, network)
        val key2 = a.requestKey(acct.state.data, network)

        val queried = a.ra().accountKeys(acct.state.data.id.id)
        assertEquals(listOf(key1, key2), queried)
    }

    @Test
    fun `should return null when key is unknown`() {
        val accountAB = a.createAndRegisterAccount(network, registry = b.identity())
        val key = a.requestKey(accountAB.state.data, network) // this key is created at NodeA, NodeB has no knowledge

        accountAB.run {
            assertEquals(a.ra().registeredAccount(key))
            assertNull(b.ra().registeredAccount(key))
        }
    }
}