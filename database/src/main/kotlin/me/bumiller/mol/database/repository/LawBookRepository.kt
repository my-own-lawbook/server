package me.bumiller.mol.database.repository

import me.bumiller.mol.common.Optional
import me.bumiller.mol.common.empty
import me.bumiller.mol.database.base.EntityRepository
import me.bumiller.mol.database.base.IEntityRepository
import me.bumiller.mol.database.table.LawBook
import me.bumiller.mol.database.table.LawBook.Entity
import me.bumiller.mol.database.table.LawBook.Model
import me.bumiller.mol.database.table.LawBook.Table
import me.bumiller.mol.database.table.LawEntry
import me.bumiller.mol.database.table.User
import me.bumiller.mol.database.util.eqOpt
import me.bumiller.mol.database.util.suspendTransaction
import org.jetbrains.exposed.sql.and

/**
 * Interface that grants access to the 'law_book' table in the database
 */
interface LawBookRepository : IEntityRepository<Long, Model> {

    /**
     * Creates a new [LawBook] in the database
     *
     * @param model The model to take data from.
     * @param creatorId The id of the user that created the [LawBook]
     *
     * @return The creates [LawBook], or null if the user was not found
     */
    suspend fun create(model: Model, creatorId: Long): Model?

    /**
     * Gets a specific [Model] matching all given criteria
     *
     * @param id The id of the law-book
     * @param creatorId The id of the user that created the law book
     * @param key The key of the law-book
     * @return The singular [Model] matching all the given criteria, or null
     */
    suspend fun getSpecific(
        id: Optional<Long> = empty(),
        key: Optional<String> = empty()
    ): Model?

    /**
     * Gets the [LawBook] that is the parent of a specific entry
     *
     * @param entryId The id of the entry
     * @return The [LawBook] that is the parent of the entry or null if the entry was not found
     */
    suspend fun getForEntry(entryId: Long): Model?

    /**
     * Gets all law-books the member is part of
     *
     * @param userId The id of the member
     * @return All law-books the user is a member of, or null if the user was not found
     */
    suspend fun getAllForMember(userId: Long): List<Model>?

}

internal class ExposedLawBookRepository : EntityRepository<Long, Model, Entity, Table, Entity.Companion>(Table, Entity),
    LawBookRepository {

    override suspend fun create(model: Model, creatorId: Long): Model? = suspendTransaction {
        val user = User.Entity.findById(creatorId) ?: return@suspendTransaction null

        Entity.new {
            creator = user
            populate(model)
        }.asModel
    }

    override suspend fun getSpecific(id: Optional<Long>, key: Optional<String>): Model? =
        suspendTransaction {
            Entity.find {
                (Table.id eqOpt id) and
                        (Table.key eqOpt key)
            }
                .singleOrNull()
                ?.asModel
        }

    override suspend fun getForEntry(entryId: Long): Model? = suspendTransaction {
        LawEntry.Entity.find {
            LawEntry.Table.id eq entryId
        }
            .singleOrNull()
            ?.parentBook?.asModel
    }

    override suspend fun getAllForMember(userId: Long): List<Model>? = suspendTransaction {
        User.Entity.find {
            User.Table.id eq userId
        }
            .singleOrNull()
            ?.lawBooks?.toList()
            ?.map { it.asModel }
    }

}