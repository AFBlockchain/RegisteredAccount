package hk.edu.polyu.af.bc.account.states

import net.corda.core.serialization.CordaSerializable
import java.util.*
import javax.persistence.Entity
import javax.persistence.Table

class StandardAccount: AccountType {
    override fun repr(): String = "stdAcct"

}

@CordaSerializable
enum class Gender {
    MALE, FEMALE
}

class StandardDetails(
    var firstName: String,
    var lastName: String,
    var gender: Gender
): AccountDetails() {
    override fun getType(): AccountType {
        return StandardAccount()
    }
}
