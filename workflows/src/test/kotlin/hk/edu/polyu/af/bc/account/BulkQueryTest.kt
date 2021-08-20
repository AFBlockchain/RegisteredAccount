package hk.edu.polyu.af.bc.account

import hk.edu.polyu.af.bc.account.flows.workflows.CreateAndRegisterAccount
import org.junit.Test

class BulkQueryTest: TestBase() {
    @Test
    fun `should query all RegisteredAccount in vault`() {
        val account1 = a.startFlow(CreateAndRegisterAccount(randomString(), randomString(), standardDetails(), b.identity())).runAndGet(network)
        val account2 = b.startFlow(CreateAndRegisterAccount(randomString(), randomString(), standardDetails(), a.identity())).runAndGet(network)

        val accounts = listOf(account1, account2)
        accounts.assertEquals(a.ra().allAccounts())
        accounts.assertEquals(b.ra().allAccounts())
    }

    @Test
    fun `should query all accounts by host`() {
        val accountAB = a.createAndRegisterAccount(network, b.identity())
        val accountBA = b.createAndRegisterAccount(network, a.identity())

        listOf(accountAB).run {
            assertEquals(a.ra().ourAccountsAsHost())
            assertEquals(b.ra().accountsByHost(a.identity()))
        }

        listOf(accountBA).run {
            assertEquals(b.ra().ourAccountsAsHost())
            assertEquals(a.ra().accountsByHost(b.identity()))
        }
    }

    @Test
    fun `should query all accounts by registry`() {
        val accountAB = a.createAndRegisterAccount(network, b.identity())
        val accountBA = b.createAndRegisterAccount(network, a.identity())

        listOf(accountAB).run {
            assertEquals(b.ra().ourAccountsAsRegistry())
            assertEquals(a.ra().accountsByRegistry(b.identity()))
        }

        listOf(accountBA).run {
            assertEquals(a.ra().ourAccountsAsRegistry())
            assertEquals(b.ra().accountsByRegistry(a.identity()))
        }
    }

    @Test
    fun `should query all accounts by type`() {
        val typeName = standardDetails().getType().repr()

        val accountAB = a.createAndRegisterAccount(network, b.identity())
        val accountBA = b.createAndRegisterAccount(network, a.identity())

        listOf(accountAB, accountBA).run {
            assertEquals(a.ra().accountsByType(typeName))
            assertEquals(b.ra().accountsByType(typeName))
        }
    }
}