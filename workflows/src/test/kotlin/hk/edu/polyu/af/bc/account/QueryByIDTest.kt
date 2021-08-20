package hk.edu.polyu.af.bc.account

import org.junit.Test
import java.util.*
import kotlin.test.assertNull

class QueryByIDTest: TestBase() {
    @Test
    fun `can query by ra-uuid`() {
        val accountAB = a.createAndRegisterAccount(network, b.identity())
        val accountBA = b.createAndRegisterAccount(network, a.identity())

        accountAB.run {
            assertEquals(a.ra().registeredAccountByRAUUID(accountAB.state.data.id.id))
            assertEquals(b.ra().registeredAccountByRAUUID(accountAB.state.data.id.id))
        }

        accountBA.run {
            assertEquals(a.ra().registeredAccountByRAUUID(accountBA.state.data.id.id))
            assertEquals(b.ra().registeredAccountByRAUUID(accountBA.state.data.id.id))
        }
    }

    @Test
    fun `can query by ai-uuid`() {
        val accountAB = a.createAndRegisterAccount(network, b.identity())
        val accountBA = b.createAndRegisterAccount(network, a.identity())

        accountAB.run {
            assertEquals(a.ra().registeredAccountByAIUUID(accountAB.state.data.aiPtr.pointer.id))
            assertEquals(b.ra().registeredAccountByAIUUID(accountAB.state.data.aiPtr.pointer.id))
        }

        accountBA.run {
            assertEquals(a.ra().registeredAccountByAIUUID(accountBA.state.data.aiPtr.pointer.id))
            assertEquals(b.ra().registeredAccountByAIUUID(accountBA.state.data.aiPtr.pointer.id))
        }
    }

    @Test
    fun `should return null where there is no match`() {
        assertNull(a.ra().registeredAccountByRAUUID(UUID.randomUUID()))
        assertNull(a.ra().registeredAccountByAIUUID(UUID.randomUUID()))
    }
}