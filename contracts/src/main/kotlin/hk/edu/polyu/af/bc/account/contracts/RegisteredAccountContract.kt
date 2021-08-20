package hk.edu.polyu.af.bc.account.contracts

import hk.edu.polyu.af.bc.account.commands.RACommand
import hk.edu.polyu.af.bc.account.commands.Register
import hk.edu.polyu.af.bc.account.states.RegisteredAccount
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction
import net.corda.core.transactions.TransactionBuilder

class RegisteredAccountContract: Contract{
    companion object {
        val ID: String = RegisteredAccountContract::class.java.canonicalName

        fun generateRegister(txb: TransactionBuilder, ra: RegisteredAccount) {
            txb.addOutputState(ra)
            txb.addCommand(Register(), ra.host.owningKey, ra.registry.owningKey)
        }
    }

    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand(RACommand::class.java)
        if (command.value is Register) {
            requireThat {
                "There should be no input RegisteredAccount state" using (tx.outputsOfType(RegisteredAccount::class.java).size == 1)
                "There should only ever be one output RegisteredAccount state" using (tx.outputStates.size == 1 && tx.outputsOfType
                    (RegisteredAccount::class.java).size == 1)

                val ra = tx.outputsOfType(RegisteredAccount::class.java).single()
                "Account's host must be the same as AccountInfo's" using (ra.host == ra.resolveAccountInfo(tx).state.data.host)

                "Registry must sign" using command.signers.contains(ra.registry.owningKey)
                "Account host must sign" using command.signers.contains(ra.host.owningKey)
            }
        } else {
            throw NotImplementedError()
        }
    }
}