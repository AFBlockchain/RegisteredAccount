package hk.edu.polyu.af.bc.account

import hk.edu.polyu.af.bc.account.flows.workflows.RequestKeyForRegisteredAccountByName
import hk.edu.polyu.af.bc.account.flows.workflows.RequestKeyForRegisteredAccountByUUID
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class RequestKeyTests: TestBase() {
    @Test
    fun `can request key for an account on the same node`() {
        val acct = a.createAndRegisterAccount(network).state.data
        val key = a.startFlow(RequestKeyForRegisteredAccountByUUID(acct.id.id)).runAndGet(network).owningKey

        assertEquals(listOf(key), a.ra().accountKeys(acct.id.id))
    }

    @Test
    fun `can request key for an account oon different nodes`() {
        val acct = a.createAndRegisterAccount(network, registry = b.identity()).state.data
        val key = b.startFlow(RequestKeyForRegisteredAccountByUUID(acct.id.id)).runAndGet(network).owningKey

        assertEquals(listOf(key), a.ra().accountKeys(acct.id.id))
        assertEquals(listOf(key), b.ra().accountKeys(acct.id.id))
    }

    @Test
    fun `can request key by name as a registry`() {
        val acct = a.createAndRegisterAccount(network, registry = b.identity()).state.data
        val key = b.startFlow(RequestKeyForRegisteredAccountByName(acct.acctName)).runAndGet(network).owningKey

        assertEquals(listOf(key), b.ra().accountKeys(acct.id.id))
    }

    @Test
    fun `request by name should only be used by registry`() {
        val name = randomString()
        val account = a.createAndRegisterAccount(network, registry = b.identity(), raName = name).state.data

        assertHaveAccount(name, a, b)

        assertThrows<IllegalArgumentException> {
            // NodeA is not registry for this account
            a.startFlow(RequestKeyForRegisteredAccountByName(name)).runAndGet(network)
        }

        // NodeB can request
        val key = b.startFlow(RequestKeyForRegisteredAccountByName(name)).runAndGet(network).owningKey
        assertEquals(listOf(key), a.ra().accountKeys(account.id.id))
        assertEquals(listOf(key), b.ra().accountKeys(account.id.id))
    }
}