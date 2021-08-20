package hk.edu.polyu.af.bc.account.flows.services

import hk.edu.polyu.af.bc.account.states.RegisteredAccount
import net.corda.core.contracts.StateAndRef
import net.corda.core.identity.Party
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SerializeAsToken
import java.security.PublicKey
import java.util.*

/**
 * 1. Bulk query
 *      - all
 *      - by host
 *      - by registry
 *      - by account type
 * 2. Query for a specific account
 *      - by RegisteredAccount's UUID
 *      - by AccountInfo's UUID
 *      - by owning key of an account
 *      - by account name
 * 3. Get keys for an account, specified by its UUID
 */

@CordaService
interface RegisteredAccountService: SerializeAsToken {
    fun allAccounts(): List<StateAndRef<RegisteredAccount>>

    fun accountsByHost(host: Party): List<StateAndRef<RegisteredAccount>>

    fun accountsByRegistry(registry: Party): List<StateAndRef<RegisteredAccount>>

    fun accountsByType(typeRepr: String): List<StateAndRef<RegisteredAccount>>

    fun ourAccountsAsHost(): List<StateAndRef<RegisteredAccount>>

    fun ourAccountsAsRegistry(): List<StateAndRef<RegisteredAccount>>

    fun registeredAccount(acctName: String): List<StateAndRef<RegisteredAccount>>

    fun registeredAccountByRAUUID(uuid: UUID): StateAndRef<RegisteredAccount>?

    fun registeredAccountByAIUUID(uuid: UUID): StateAndRef<RegisteredAccount>?

    fun registeredAccount(owningKey: PublicKey): StateAndRef<RegisteredAccount>?

    fun accountKeys(raUUID: UUID): List<PublicKey>
}