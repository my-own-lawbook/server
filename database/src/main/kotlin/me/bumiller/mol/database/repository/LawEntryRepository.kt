package me.bumiller.mol.database.repository

import me.bumiller.mol.common.Optional
import me.bumiller.mol.common.empty
import me.bumiller.mol.database.base.EntityRepository
import me.bumiller.mol.database.base.IEntityRepository
import me.bumiller.mol.database.table.LawBook
import me.bumiller.mol.database.table.LawEntry
import me.bumiller.mol.database.table.LawEntry.Entity
import me.bumiller.mol.database.table.LawEntry.Model
import me.bumiller.mol.database.table.LawEntry.Table
import me.bumiller.mol.database.table.LawSection
import me.bumiller.mol.database.util.eqOpt
import me.bumiller.mol.database.util.suspendTransaction
import org.jetbrains.exposed.sql.and

/**
 * Repository that grants access to the 'law_entry' table
 */
interface LawEntryRepository : IEntityRepository<Long, Model> {

    /**
     * Creates a new [LawEntry]
     *
     * @param model The model to take the data from
     * @param bookId The id of the parent book
     * @return The created model
     */
    suspend fun create(model: Model, bookId: Long): Model?

    /**
     * Updates the parent book of the entry
     *
     * @param entryId The id of the entry of which to change the parent book
     * @param parentBookId The id of the new parent book
     * @return The updated [Model], or null if either the entry or the new parent book were not found
     */
    suspend fun updateParentBook(entryId: Long, parentBookId: Long): Model?

    /**
     * Gets a specific [Model] matching all the given criteria
     *
     * @param id The id of the entry
     * @param key The key of the entry
     * @param parentBookId The id of the parent law-book
     * @return The singular [Model] matching all given criteria, or null
     */
    suspend fun getSpecific(
        id: Optional<Long> = empty(), key: Optional<String> = empty(), parentBookId: Optional<Long> = empty()
    ): Model?

    /**
     * Gets all [Model]s for the specified parent book
     *
     * @param parentBookId The id of the parent book
     * @return All [Model]s in the parent book
     */
    suspend fun getForParentBook(parentBookId: Long): List<Model>

    /**
     * Gets the [LawEntry] that is the parent of a specific section
     *
     * @param sectionId The id of the section
     * @return The [LawEntry] that is the parent of the section, or null if the section was not found
     */
    suspend fun getForSection(sectionId: Long): Model?

}

internal class ExposedLawEntryRepository :
    EntityRepository<Long, Model, Entity, Table, Entity.Companion>(Table, Entity), LawEntryRepository {

    override suspend fun create(model: Model, bookId: Long): Model? = suspendTransaction {
        val book = LawBook.Entity.findById(bookId) ?: return@suspendTransaction null
        Entity.new {
            parentBook = book
            populate(model)
        }.asModel
    }

    override suspend fun updateParentBook(entryId: Long, parentBookId: Long): Model? = suspendTransaction {
        val parentBook = LawBook.Entity.findById(parentBookId)
        parentBook?.let {
            Entity.findByIdAndUpdate(entryId) {
                it.parentBook = parentBook
            }
        }?.asModel
    }

    override suspend fun getSpecific(id: Optional<Long>, key: Optional<String>, parentBookId: Optional<Long>): Model? =
        suspendTransaction {
            Entity.find {
                (Table.id eqOpt id) and (Table.key eqOpt key) and (Table.parentBook eqOpt parentBookId)
            }.singleOrNull()?.asModel
        }

    override suspend fun getForParentBook(parentBookId: Long): List<Model> = suspendTransaction {
        Entity.find {
            Table.parentBook eq parentBookId
        }.map { it.asModel }
    }

    override suspend fun getForSection(sectionId: Long): Model? = suspendTransaction {
        LawSection.Entity.find {
            LawSection.Table.id eq sectionId
        }
            .singleOrNull()
            ?.parentEntry?.asModel
    }

}