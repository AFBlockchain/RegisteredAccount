package hk.edu.polyu.af.bc.account.flows

import net.corda.core.flows.FlowLogic

fun FlowLogic<*>.defaultNotary() = serviceHub.networkMapCache.notaryIdentities.first()