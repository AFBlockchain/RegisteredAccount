package hk.edu.polyu.af.bc.account

import hk.edu.polyu.af.bc.account.flows.services.RegisteredAccountValidationService
import hk.edu.polyu.af.bc.account.states.RegisteredAccount
import net.corda.core.flows.FlowException
import org.junit.Test
import org.junit.jupiter.api.assertThrows

class ValidationTest: TestBase() {
    @Test
    fun `should reject account with MAGIC_STRING`() {
        assertThrows<FlowException> {
            a.createAndRegisterAccount(network, registry = a.identity(), raName = TestValidator.MAGIC_STRING)
        }
    }
}

class TestValidator: RegisteredAccountValidationService {
    companion object {
        const val MAGIC_STRING = "ALOHA"
    }
    override fun validate(ra: RegisteredAccount) {
        if (ra.acctName == MAGIC_STRING) throw FlowException("Reject account with name $MAGIC_STRING")
    }
}