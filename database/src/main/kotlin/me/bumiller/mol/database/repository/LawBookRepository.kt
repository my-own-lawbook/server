package me.bumiller.mol.database.repository

import me.bumiller.mol.common.Optional
import me.bumiller.mol.common.empty
import me.bumiller.mol.database.base.EntityRepository
import me.bumiller.mol.database.base.IEntityRepository
import me.bumiller.mol.database.table.LawBook.Entity
import me.bumiller.mol.database.table.LawBook.Model
import me.bumiller.mol.database.table.LawBook.Table
import me.bumiller.mol.database.table.User
import me.bumiller.mol.database.util.eqOpt
import me.bumiller.mol.database.util.suspendTransaction
import org.jetbrains.exposed.sql.and

/**
 * Interface that grants access to the 'law_book' table in the database
 */
interface LawBookRepository : IEntityRepository<Long, Model> {

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
        creatorId: Optional<Long> = empty(),
        key: Optional<String> = empty()
    ): Model?

    /**
     * Gets all the [Model]s that were created by a specific creator
     *
     * @param creatorId The id of the creator
     * @return All matching models
     */
    suspend fun getForCreator(creatorId: Long): List<Model>

}

internal class ExposedLawBookRepository : EntityRepository<Long, Model, Entity, Table, Entity.Companion>(Table, Entity),
    LawBookRepository {

    override fun populateEntity(entity: Entity, model: Model): Entity = entity.apply {
        val userEntity = User.Entity.findById(entity.creator.id)!!
        populate(model, userEntity)
    }

    override fun map(entity: Entity): Model = entity.asModel

    override suspend fun getSpecific(id: Optional<Long>, creatorId: Optional<Long>, key: Optional<String>): Model? =
        suspendTransaction {
            Entity.find {
                (Table.id eqOpt id) and
                        (Table.creator eqOpt creatorId) and
                        (Table.key eqOpt key)
            }
                .singleOrNull()
                ?.let(::map)
        }

    override suspend fun getForCreator(creatorId: Long): List<Model> = suspendTransaction {
        Entity.find {
            Table.creator eq creatorId
        }
            .map(::map)
    }

}