package hk.edu.polyu.af.bc.account

import org.junit.Test

class QueryByNameTest: TestBase() {
    @Test
    fun `should query one account`() {
        val acct = a.createAndRegisterAccount(network, b.identity())

        listOf(acct).run {
            assertEquals(a.ra().registeredAccount(acct.state.data.acctName))
            assertEquals(b.ra().registeredAccount(acct.state.data.acctName))
        }
    }

    @Test
    fun `should return empty list when there is none`() {
        val bogusName = randomString()

        assert(a.ra().registeredAccount(bogusName).isEmpty())
    }

    @Test
    fun `should return multiple accounts when there is name conflict`() {
        val name = randomString()

        val accountAB = a.createAndRegisterAccount(network, registry = b.identity(), raName = name)
        val accountBA = b.createAndRegisterAccount(network, registry = a.identity(), raName = name)

        listOf(accountAB, accountBA).run {
            assertEquals(a.ra().registeredAccount(name))
            assertEquals(b.ra().registeredAccount(name))
        }
    }
}