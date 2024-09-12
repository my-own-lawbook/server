package me.bumiller.mol.database.base

import me.bumiller.mol.database.util.suspendTransaction
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.dao.Entity as ExposedEntity

interface IEntityRepository<Id : Comparable<Id>, Model : BaseModel<Id>> {

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
abstract class EntityRepository<
        Id : Comparable<Id>,
        Model : BaseModel<Id>,
        Entity,
        Table : IdTable<Id>,
        Class : EntityClass<Id, Entity>
        >(

    val table: Table,

    val entityClass: Class

) : IEntityRepository<Id, Model> where Entity : ExposedEntity<Id>, Entity : ModelMappableEntity<Id, Model> {

    override suspend fun getAll(): List<Model> = suspendTransaction {
        entityClass.all().map { it.asModel }
    }

    override suspend fun getSpecific(id: Id): Model? = suspendTransaction {
        entityClass.findById(id)?.asModel
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
            it.populate(model)
        }?.asModel
    }

}