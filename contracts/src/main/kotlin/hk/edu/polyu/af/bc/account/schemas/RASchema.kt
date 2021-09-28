package hk.edu.polyu.af.bc.account.schemas

import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import org.hibernate.Hibernate
import java.security.cert.X509Certificate
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table
import javax.persistence.UniqueConstraint

open class RASchema

class RASchemaV1: MappedSchema(
    RASchema::class.java, 1,
    listOf(PersistentRA::class.java)
)

@Entity
@Table(
    name = "registered_accounts",
    uniqueConstraints = [
        UniqueConstraint(name = "id_constraint", columnNames = ["id"]), // universally unique id
        UniqueConstraint(name = "account_info_constraint", columnNames = ["account_info_id"]), // one [AccountInfo] for one [RegisteredAccount]
        UniqueConstraint(name = "registry_and_name_constraint", columnNames = ["registry", "account_name"]) // unique account name under a registry
    ]
)
data class PersistentRA(
    @Column(name = "id", unique = true, nullable = false, columnDefinition = "varbinary not null")
    val raUUID: UUID = UUID.randomUUID(),

    @Column(name = "account_info_id", unique = true, nullable = false, columnDefinition = "varbinary not null")
    val aiUUID: UUID = UUID.randomUUID(),

    @Column(name = "account_name", unique = false, nullable = false)
    val acctName: String = "",

    @Column(name = "account_type", unique = false, nullable = false)
    val accountType: String = "",

    @Column(name = "host", nullable = false)
    val host: Party? = null,

    @Column(name = "registry", nullable = false)
    val registry: Party? = null,
): PersistentState() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as PersistentRA

        return stateRef != null && stateRef == other.stateRef
    }

    override fun hashCode(): Int = stateRef?.hashCode() ?: 0

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(EmbeddedId = $stateRef , raUUID = $raUUID , aiUUID = $aiUUID , acctName = $acctName , accountType = $accountType , host = $host , registry = $registry )"
    }
}