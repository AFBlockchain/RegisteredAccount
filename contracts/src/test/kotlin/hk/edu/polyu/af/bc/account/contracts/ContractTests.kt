package hk.edu.polyu.af.bc.account.contracts

import com.r3.corda.lib.accounts.contracts.commands.Create
import com.r3.corda.lib.accounts.contracts.states.AccountInfo
import hk.edu.polyu.af.bc.account.commands.Register
import hk.edu.polyu.af.bc.account.states.AccountDetails
import hk.edu.polyu.af.bc.account.states.Gender
import hk.edu.polyu.af.bc.account.states.RegisteredAccount
import hk.edu.polyu.af.bc.account.states.StandardDetails
import net.corda.core.contracts.*
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.node.NetworkParameters
import net.corda.testing.core.TestIdentity
import net.corda.testing.dsl.LedgerDSL
import net.corda.testing.dsl.TransactionDSL
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import org.junit.Test
import java.util.*


class ContractTests {
    companion object {
        const val raPkg = "hk.edu.polyu.af.bc.account"
        const val accountSDKPkg = "com.r3.corda.lib.accounts"
        const val sdkContractClassName = "$accountSDKPkg.contracts.AccountInfoContract"
    }

    // to configure minimumPlatformVersion to 4 in order to use reference states in transactions
    private val networkParameters = NetworkParameters(4, emptyList(), 10485760, 524288000, java.time.Instant.now(), 1, emptyMap<String, MutableList<out SecureHash>>())

    private val a = TestIdentity(CordaX500Name("PolyU", "Kowloon", "HK"))
    private val b = TestIdentity(CordaX500Name("R3LLC", "NYC", "US"))

    private val ledgerServices = MockServices(listOf(accountSDKPkg, raPkg), a, networkParameters, b)

    private lateinit var caLbl: String
    private lateinit var infoRef: StateAndRef<AccountInfo>
    private lateinit var raLbl: String
    private lateinit var raRef: StateAndRef<RegisteredAccount>


    @Test
    fun registerTests() {
        ledgerServices.ledger {
            createAccountInfo(a.party)

            // Happy path
            var ra: RegisteredAccount = registeredAccount()
            transaction {
                outputR(RegisteredAccountContract.ID, randomString().also {raLbl = it}, ra)
                command(a.publicKey, Register())
                verifies()
            }

            // Host conflict. [AccountInfo]'s host is NodeA, whereas declared host is NodeB
            ra = registeredAccount(host = b.party, registry = b.party)
            transaction {
                outputR(RegisteredAccountContract.ID, randomString().also {raLbl = it}, ra)
                command(b.publicKey, Register())
                fails()
            }

            // Not enough signatures
            ra = registeredAccount(host = a.party, registry = b.party)
            transaction {
                outputR(RegisteredAccountContract.ID, randomString().also {raLbl = it}, ra)

                tweak { // lacking host's signature
                    command(b.publicKey, Register())
                    fails()
                }

                tweak { // lacking registry's signature
                    command(b.publicKey, Register())
                    fails()
                }

                command(listOf(a.publicKey, b.publicKey), Register())
                verifies()
            }
        }
    }

    /**
     * Wrapper to automatically add the referenced [AccountInfo] to the transaction
     */
    private fun TransactionDSL<*>.outputR(contractClassName: ContractClassName, label: String, contractState: ContractState){
        output(contractClassName, label, contractState)
        reference(caLbl)
    }

    /**
     * Create a random [RegisteredAccount] that points to infoRef at the given registry. [AccountDetails] implementation is
     * [StandardDetails]. Null parameter means the registry is the account's host
     */
    private fun registeredAccount(host: Party? = null, registry: Party? = null) =
        RegisteredAccount(
            LinearPointer(infoRef.state.data.identifier, AccountInfo::class.java),
            randomString(), StandardDetails(randomString(),randomString(),Gender.FEMALE),
            host ?: infoRef.state.data.host,
            registry ?: infoRef.state.data.host)

    /**
     * Commit an [AccountInfo] to ledger and update references accordingly: label and StateAndRef
     */
    private fun LedgerDSL<*,*>.createAccountInfo(host: Party) {
        transaction {
            val info = AccountInfo(randomString(), host, UniqueIdentifier())
            output(sdkContractClassName, randomString().also { caLbl = it } , info)
            command(a.publicKey, Create())
            verifies()
        }

        infoRef = retrieveOutputStateAndRef(AccountInfo::class.java, caLbl)
    }

    private fun randomString() = UUID.randomUUID().toString()
}