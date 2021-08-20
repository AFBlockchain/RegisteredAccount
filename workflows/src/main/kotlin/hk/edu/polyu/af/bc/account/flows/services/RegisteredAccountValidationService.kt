package hk.edu.polyu.af.bc.account.flows.services

import hk.edu.polyu.af.bc.account.states.RegisteredAccount
import net.corda.core.flows.FlowException

class RegisteredAccountValidationException(msg: String): FlowException(msg)

interface RegisteredAccountValidationService {
    fun validate(ra: RegisteredAccount)
}