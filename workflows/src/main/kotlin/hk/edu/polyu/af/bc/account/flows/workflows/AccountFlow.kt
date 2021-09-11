package hk.edu.polyu.af.bc.account.flows.workflows

import net.corda.core.flows.FlowLogic

/**
 * Toy flow returning the module information
 */
class AccountFlow: FlowLogic<String>(){
    override fun call(): String {
        return "The Account Module"
    }
}