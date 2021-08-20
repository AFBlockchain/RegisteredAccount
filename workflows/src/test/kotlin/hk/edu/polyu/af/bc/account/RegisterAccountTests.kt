package hk.edu.polyu.af.bc.account

import hk.edu.polyu.af.bc.account.flows.workflows.CreateAndRegisterAccount
import hk.edu.polyu.af.bc.account.flows.workflows.RegisterAccount
import hk.edu.polyu.af.bc.account.flows.workflows.RegisterAccountBase
import net.corda.core.flows.FlowException
import org.junit.Test
import org.junit.jupiter.api.assertThrows


class RegisterAccountTests: TestBase(){
    @Test
    fun `should host and register account at the same node`() {
        // hosted by & registered at NodeA
        val infoSaf = a.accountInfo(network)
        val ra = infoSaf.registeredAccount()

        a.startFlow(RegisterAccountBase(ra)).runAndGet(network)

        assertHaveAccount(ra, a)
    }

    @Test
    fun `should host account at a node and register at a different node`() {
        // hosted by NodeA & registered at NodeB
        val infoSaf = a.accountInfo(network)
        val ra = infoSaf.registeredAccount(registry = b.identity())

        a.startFlow(RegisterAccountBase(ra)).runAndGet(network)

        assertHaveAccount(ra, a, b)
    }

    @Test
    fun `should fail at host due to unique RA-UUID constraint`() {
        val infoSaf1 = a.accountInfo(network)
        val infoSaf2 = a.accountInfo(network)
        val ra1 = infoSaf1.registeredAccount()
        val ra2 = infoSaf2.registeredAccount(uid = ra1.id)

        a.startFlow(RegisterAccountBase(ra1)).runAndGet(network) // this is fine
        assertHaveAccount(ra1, a)

        assertThrows<FlowException> {
            a.startFlow(RegisterAccountBase(ra2)).runAndGet(network)
        }
    }

    @Test
    fun `should fail at registry due to unique RA-UUID constraint`() {
        val infoSaf1 = a.accountInfo(network)
        val infoSaf2 = b.accountInfo(network)
        val ra1 = infoSaf1.registeredAccount(registry = a.identity())
        val ra2 = infoSaf2.registeredAccount(uid = ra1.id, registry = a.identity())

        a.startFlow(RegisterAccountBase(ra1)).runAndGet(network) // this is fine
        assertHaveAccount(ra1, a)

        assertThrows<FlowException> {
            b.startFlow(RegisterAccountBase(ra2)).runAndGet(network)
        }
    }

    @Test
    fun `should fail at host due to unique AI-UUID constraint`() {
        val infoSaf = a.accountInfo(network)
        val ra1 = infoSaf.registeredAccount()
        val ra2 = infoSaf.registeredAccount()

        a.startFlow(RegisterAccountBase(ra1)).runAndGet(network) // this is fine
        assertHaveAccount(ra1, a)

        assertThrows<FlowException> {
            a.startFlow(RegisterAccountBase(ra2)).runAndGet(network)
        }
    }

    @Test
    fun `cannot register different accounts backed by the same AccountInfo even at different registry`() {
        val infoSaf = a.accountInfo(network)
        val ra1 = infoSaf.registeredAccount(registry = a.identity())
        val ra2 = infoSaf.registeredAccount(registry = b.identity())

        a.startFlow(RegisterAccountBase(ra1)).runAndGet(network) // this is fine
        assertHaveAccount(ra1, a)

        assertThrows<FlowException> {
            a.startFlow(RegisterAccountBase(ra2)).runAndGet(network)
        }
    }

    @Test
    fun `should fail at host due to unique name constraint`() {
        val name = "Non-Unique-Account-Name"
        val infoSaf1 = a.accountInfo(network)
        val infoSaf2 = a.accountInfo(network)
        val ra1 = infoSaf1.registeredAccount(name = name)
        val ra2 = infoSaf2.registeredAccount(name = name)

        a.startFlow(RegisterAccountBase(ra1)).runAndGet(network) // this is fine
        assertHaveAccount(ra1, a)

        assertThrows<FlowException> {
            a.startFlow(RegisterAccountBase(ra2)).runAndGet(network)
        }
    }

    @Test
    fun `should fail at registry due to unique name constraint`() {
        val name = "Non-Unique-Account-Name"
        val infoSaf1 = a.accountInfo(network)
        val infoSaf2 = b.accountInfo(network)
        val ra1 = infoSaf1.registeredAccount(name = name, registry = b.identity())
        val ra2 = infoSaf2.registeredAccount(name = name, registry = b.identity())

        b.startFlow(RegisterAccountBase(ra2)).runAndGet(network) // NodeB registers first
        assertHaveAccount(ra2, b)

        assertThrows<FlowException> {
            // NodeA doesn't know there's already an account using the name. Tx fails at counter flow
            a.startFlow(RegisterAccountBase(ra1)).runAndGet(network)
        }
    }

    @Test
    fun `can create account with same name at different registry`() {
        val name = "Non-Unique-Account-Name"
        val infoSaf1 = a.accountInfo(network)
        val infoSaf2 = a.accountInfo(network)
        val ra1 = infoSaf1.registeredAccount(registry = a.identity(), name = name)
        val ra2 = infoSaf2.registeredAccount(registry = b.identity(), name = name)

        a.startFlow(RegisterAccountBase(ra1)).runAndGet(network)
        assertHaveAccount(ra1, a)

        a.startFlow(RegisterAccountBase(ra2)).runAndGet(network)
        assertHaveAccount(ra2, a, b) // b also has this account as the registry
    }

    @Test
    fun `can use RegisterAccount flow at the same node`() {
        val name = randomString()
        val infoSaf = a.accountInfo(network)

        a.startFlow(RegisterAccount(infoSaf.state.data.linearId, name, standardDetails())).runAndGet(network)
        assertHaveAccount(name, a)
    }

    @Test
    fun `can use RegisterAccount flow at different nodes`() {
        val name = randomString()
        val infoSaf = a.accountInfo(network)

        a.startFlow(RegisterAccount(infoSaf.state.data.linearId, name, standardDetails(), b.identity())).runAndGet(network)
        assertHaveAccount(name, a, b)
    }

    @Test
    fun `can create and register account at the same node`() {
        val name = randomString()

        a.startFlow(CreateAndRegisterAccount(name, name, standardDetails())).runAndGet(network)
        assertHaveAccount(name, a)
    }

    @Test
    fun `can create and register account at the different nodes`() {
        val name = randomString()

        a.startFlow(CreateAndRegisterAccount(name, name, standardDetails(), b.identity())).runAndGet(network)
        assertHaveAccount(name, a, b)
    }
}