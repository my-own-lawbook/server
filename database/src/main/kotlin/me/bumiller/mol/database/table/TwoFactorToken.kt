package me.bumiller.mol.database.table

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.bumiller.mol.database.base.BaseModel
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import java.util.*

object TwoFactorToken {

    /**
     * Model that represents a row in the 'two_factor_token' table
     */
    data class Model(

        /**
         * The id
         */
        override val id: Long,

        /**
         * The actual token
         */
        val token: UUID,

        /**
         * When it wa issued
         */
        val issuedAt: Instant,

        /**
         * When it is expiring.
         *
         * If null, has no expiration date.
         */
        val expiringAt: Instant?,

        /**
         * Whether this has already been used
         */
        val used: Boolean,

        /**
         * Optional additional content
         */
        val additionalContent: String?,

        /**
         * THe usage type for this token
         */
        val type: String,

        /**
         * The user this token belongs to
         */
        val user: User.Model

    ) : BaseModel<Long>

    internal object Table : LongIdTable("two_factor_token") {

        val user = reference("user_id", User.Table)

        val token = uuid("token").uniqueIndex()

        val issuedAt = timestamp("issued_at").clientDefault { Clock.System.now() }

        val expiringAt = timestamp("expiring_at").nullable()

        val used = bool("used").default(false)

        val type = text("token_type")

        val additionalContent = text("additional_content").nullable()

    }

    internal class Entity(id: EntityID<Long>) : LongEntity(id) {

        var user by User.Entity referencedOn Table.user
        var token by Table.token
        var issuedAt by Table.issuedAt
        var expiringAt by Table.expiringAt
        var used by Table.used
        var additionalContent by Table.additionalContent
        var type by Table.type

        val asModel
            get() = Model(id.value, token, issuedAt, expiringAt, used, additionalContent, type, user.asModel)

        fun populate(model: Model) {
            user = User.Entity(EntityID(model.user.id, User.Table)).apply {
                populate(model.user)
            }
            issuedAt = model.issuedAt
            expiringAt = model.expiringAt
            used = model.used
            token = model.token
            type = model.type
            additionalContent = model.additionalContent
        }

        companion object : LongEntityClass<Entity>(Table)

    }

}




