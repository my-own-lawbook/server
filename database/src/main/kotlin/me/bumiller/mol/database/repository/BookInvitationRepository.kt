package me.bumiller.mol.database.repository

import kotlinx.datetime.Clock
import me.bumiller.mol.database.base.EntityRepository
import me.bumiller.mol.database.base.IEntityRepository
import me.bumiller.mol.database.table.BookInvitation
import me.bumiller.mol.database.table.BookInvitation.Entity
import me.bumiller.mol.database.table.BookInvitation.Model
import me.bumiller.mol.database.table.BookInvitation.Table
import me.bumiller.mol.database.table.LawBook
import me.bumiller.mol.database.table.User
import me.bumiller.mol.database.util.suspendTransaction
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greater
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull

/**
 * Interface to access the "book_invitations" table in the database
 */
interface BookInvitationRepository : IEntityRepository<Long, Model> {

    /**
     * Creates a new book invitation row
     *
     * @param model The model to take the data from
     * @param authorId The id of the author of the invitation
     * @param targetBookId The id of the target book
     * @param recipientId The id of the recipient
     * @return The created [Model], or null if either the author, the book or the recipient were not found.
     */
    suspend fun create(model: Model, authorId: Long, targetBookId: Long, recipientId: Long): Model?

    /**
     * Gets all the [Model]s matching all the given queries
     *
     * @param authorId The id of the author
     * @param targetBookId The id of the target book
     * @param recipientId The id of the recipient
     * @param statuses The statuses to filter for
     * @param onlyNonExpired Whether to only return invitations that are expired
     * @return All [Model]s matching the given queries
     */
    suspend fun getAll(
        authorId: Long? = null,
        targetBookId: Long? = null,
        recipientId: Long? = null,
        statuses: List<BookInvitation.Status> = BookInvitation.Status.entries,
        onlyNonExpired: Boolean = false
    ): List<Model>

}

internal class ExposedBookInvitationRepository : BookInvitationRepository,
    EntityRepository<Long, Model, Entity, Table, Entity.Companion>(Table, Entity) {

    override suspend fun create(model: Model, authorId: Long, targetBookId: Long, recipientId: Long) =
        suspendTransaction {
            val author = User.Entity.findById(authorId) ?: return@suspendTransaction null
            val targetBook = LawBook.Entity.findById(targetBookId) ?: return@suspendTransaction null
            val recipient = User.Entity.findById(recipientId) ?: return@suspendTransaction null

            Entity.new {
                this.author = author
                this.targetBook = targetBook
                this.recipient = recipient
                populate(model)
            }.asModel
        }

    override suspend fun getAll(
        authorId: Long?,
        targetBookId: Long?,
        recipientId: Long?,
        statuses: List<BookInvitation.Status>,
        onlyNonExpired: Boolean
    ): List<Model> = suspendTransaction {
        Entity.find {
            (authorId?.let { Table.author eq authorId } ?: Op.TRUE) andIfNotNull
                    targetBookId?.let { Table.targetBook eq targetBookId } andIfNotNull
                    recipientId?.let { Table.recipient eq recipientId } and
                    (Table.status inList statuses) and
                    nonExpiredOp(onlyNonExpired)
        }.map(Entity::asModel)
    }

    private fun nonExpiredOp(onlyNonExpired: Boolean): Expression<Boolean> {
        if (!onlyNonExpired) return Op.TRUE

        return Table.expiredTimestamp.isNull() or (Table.expiredTimestamp greater Clock.System.now())
    }

}