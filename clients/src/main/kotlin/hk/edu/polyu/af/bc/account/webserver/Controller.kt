package hk.edu.polyu.af.bc.account.webserver

import com.r3.corda.lib.accounts.workflows.flows.OurAccounts
import hk.edu.polyu.af.bc.account.flows.workflows.OurRegisteredAccountsAsHost
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Define your API endpoints here.
 */
@RestController
class Controller(rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy

    @GetMapping("/accounts")
    fun getAccounts(): List<String> {
        return proxy.startFlowDynamic(OurRegisteredAccountsAsHost::class.java).returnValue.get().map { it.state.data.acctName }
    }
}