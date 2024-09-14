package me.bumiller.mol.database.repository

import me.bumiller.mol.common.Optional
import me.bumiller.mol.common.empty
import me.bumiller.mol.database.base.EntityRepository
import me.bumiller.mol.database.base.IEntityRepository
import me.bumiller.mol.database.table.LawEntry
import me.bumiller.mol.database.table.LawSection
import me.bumiller.mol.database.table.LawSection.Entity
import me.bumiller.mol.database.table.LawSection.Model
import me.bumiller.mol.database.table.LawSection.Table
import me.bumiller.mol.database.util.eqOpt
import me.bumiller.mol.database.util.suspendTransaction
import org.jetbrains.exposed.sql.and

/**
 * Repository that grants access to the 'law_section' table in the database
 */
interface LawSectionRepository : IEntityRepository<Long, Model> {

    /**
     * Creates a new [LawSection]
     *
     * @param model The model to take the data from
     * @return The created [LawSection]
     */
    suspend fun create(model: Model): Model

    /**
     * Updates the parent entry of a [Model]
     *
     * @param sectionId The id of the section to update
     * @param parentEntryId The id of the new parent entry
     * @return The [Model] that was updated, or null if either the section or the entry was not found
     */
    suspend fun updateParentEntry(sectionId: Long, parentEntryId: Long): Model?

    /**
     * Gets a specific [Model] that matches all the given criteria
     *
     * @param id The id of the [Model]
     * @param index The index of the [Model]
     * @param parentEntryId The id of the parent entry of the [Model]
     * @return The singular model matching all given criteria, or null
     */
    suspend fun getSpecific(
        id: Optional<Long> = empty(),
        index: Optional<String> = empty(),
        parentEntryId: Optional<Long> = empty()
    ): Model?

    /**
     * Gets all [Model]s for a specific parent entry
     *
     * @param parentEntryId The id of the entry
     * @return All models in the given entry
     */
    suspend fun getForParentEntry(parentEntryId: Long): List<Model>
}

internal class ExposedLawSectionRepository :
    EntityRepository<Long, Model, Entity, Table, Entity.Companion>(Table, Entity), LawSectionRepository {

    override suspend fun getSpecific(
        id: Optional<Long>,
        index: Optional<String>,
        parentEntryId: Optional<Long>
    ): Model? = suspendTransaction {
        Entity.find {
            (Table.id eqOpt id) and
                    (Table.index eqOpt index) and
                    (Table.parentEntry eqOpt parentEntryId)
        }
            .singleOrNull()
            ?.asModel
    }

    override suspend fun create(model: Model): Model = suspendTransaction {
        Entity.new {
            populate(model)
        }.asModel
    }

    override suspend fun updateParentEntry(sectionId: Long, parentEntryId: Long): Model? = suspendTransaction {
        val parentEntry = LawEntry.Entity.findById(parentEntryId)
        parentEntry?.let {
            Entity.findByIdAndUpdate(sectionId) {
                it.parentEntry = parentEntry
            }
        }?.asModel
    }

    override suspend fun getForParentEntry(parentEntryId: Long): List<Model> = suspendTransaction {
        Entity.find {
            Table.parentEntry eq parentEntryId
        }
            .map { it.asModel }
    }
}