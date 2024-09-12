package me.bumiller.mol.database.base

import me.bumiller.mol.database.util.suspendTransaction
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.dao.Entity as ExposedEntity

interface IEntityRepository<Id : Comparable<Id>, Model> {

    /**
     * Gets all models
     *
     * @return All models
     */
    suspend fun getAll(): List<Model>

    /**
     * Gets a specific row of the database
     *
     * @param id The id of the model
     * @return The model, or null if it was not found
     */
    suspend fun getSpecific(id: Id): Model?

    /**
     * Creates a new model
     *
     * The id column is ignored
     *
     * @param model The model
     * @return The created model
     */
    suspend fun create(model: Model): Model

    /**
     * Deletes a specific model
     *
     * @param id The id of the model to delete
     * @return The deleted model, or null if it was not found
     */
    suspend fun delete(id: Id): Model?

    /**
     * Updates a model
     *
     * @param model The model to update. Matched by the id
     * @return The updated model, or null if it was not found
     */
    suspend fun update(model: Model): Model?

}

/**
 * Base interface providing simple CRUD methods to a repository
 */
abstract class EntityRepository<Id : Comparable<Id>, Model : BaseModel<Id>, Entity : ExposedEntity<Id>, Table : IdTable<Id>, Class : EntityClass<Id, Entity>>(

    val table: Table,

    val entityClass: Class

) : IEntityRepository<Id, Model> {

    /**
     * Copy the properties from the model to the entity class
     *
     * @param entity The dao to populate
     * @param model The entity to take the traits from
     * @param exists Whether the entity already exists, i.e. is being updated or just created. Useful when the [entity] will also be populated by values that are retrieved from the database.
     * @return The populated dao
     */
    abstract fun populateEntity(entity: Entity, model: Model, exists: Boolean): Entity

    /**
     * Map an entity to its corresponding model
     *
     * @param entity The entity
     * @return The model
     */
    abstract fun map(entity: Entity): Model

    override suspend fun getAll(): List<Model> = suspendTransaction {
        entityClass.all().map(::map)
    }

    override suspend fun getSpecific(id: Id): Model? = suspendTransaction {
        entityClass.findById(id)?.let(::map)
    }

    override suspend fun create(model: Model): Model = suspendTransaction {
        entityClass.new {
            populateEntity(this, model, false)
        }.let(::map)
    }

    override suspend fun delete(id: Id): Model? = suspendTransaction {
        val model = getSpecific(id)
        model.also {
            table.deleteWhere {
                table.id eq id
            }
        }
    }

    override suspend fun update(model: Model): Model? = suspendTransaction {
        entityClass.findByIdAndUpdate(model.id) {
            populateEntity(it, model, false)
        }?.let(::map)
    }

}