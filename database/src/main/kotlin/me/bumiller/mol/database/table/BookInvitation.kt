package me.bumiller.mol.database.table

import kotlinx.datetime.Instant
import me.bumiller.mol.database.base.BaseModel
import me.bumiller.mol.database.base.ModelMappableEntity
import me.bumiller.mol.database.table.crossref.LawBookMembersCrossref
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object BookInvitation {

    enum class Status {

        Open,

        Accepted,

        Denied,

        Revoked

    }

    data class Model(

        override val id: Long,

        val author: User.Model,

        val targetBook: LawBook.Model,

        val recipient: User.Model,

        val role: String,

        val sentAt: Instant,

        val usedAt: Instant?,

        val status: Status,

        val expiresAt: Instant?,

        val message: String?

    ) : BaseModel<Long>

    object Table : LongIdTable(name = "book_invitations") {

        val author = reference("author_id", User.Table, ReferenceOption.CASCADE)

        val targetBook = reference("book_id", LawBook.Table, ReferenceOption.CASCADE)

        val recipient = reference("recipient_id", User.Table, ReferenceOption.CASCADE)

        val role = text("role").check("role-valid") {
            it inList LawBookMembersCrossref.Roles.entries.map(LawBookMembersCrossref.Roles::serializedName)
        }

        val sentTimestamp = timestamp("invited_at")

        val usedTimestamp = timestamp("used_at").nullable()

        val status = enumeration<Status>("status")

        val expiredTimestamp = timestamp("expired_at").nullable()

        val message = text("message").nullable()

    }

    internal class Entity(id: EntityID<Long>) : LongEntity(id), ModelMappableEntity<Model> {

        var author by User.Entity referencedOn Table.author
        var targetBook by LawBook.Entity referencedOn Table.targetBook
        var recipient by User.Entity referencedOn Table.recipient

        var role by Table.role
        var sentAt by Table.sentTimestamp
        var usedAt by Table.usedTimestamp
        var status by Table.status
        var expiresAt by Table.expiredTimestamp
        var message by Table.message

        override val asModel: Model
            get() = Model(
                id.value,
                author.asModel,
                targetBook.asModel,
                recipient.asModel,
                role,
                sentAt,
                usedAt,
                status,
                expiresAt,
                message
            )

        override fun populate(model: Model) {
            author = User.Entity.findById(model.author.id)!!
            targetBook = LawBook.Entity.findById(model.targetBook.id)!!
            recipient = User.Entity.findById(model.recipient.id)!!

            role = model.role
            sentAt = model.sentAt
            usedAt = model.usedAt
            status = model.status
            expiresAt = model.expiresAt
            message = model.message
        }

        companion object : LongEntityClass<Entity>(Table)

    }

}