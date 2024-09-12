package me.bumiller.mol.database.repository

import me.bumiller.mol.common.Optional
import me.bumiller.mol.common.empty
import me.bumiller.mol.database.base.EntityRepository
import me.bumiller.mol.database.base.IEntityRepository
import me.bumiller.mol.database.table.User.Entity
import me.bumiller.mol.database.table.User.Model
import me.bumiller.mol.database.table.User.Table
import me.bumiller.mol.database.table.UserProfile
import me.bumiller.mol.database.util.eqOpt
import me.bumiller.mol.database.util.suspendTransaction
import org.jetbrains.exposed.sql.and

/**
 * Repository to access the records in the users table
 */
interface UserRepository : IEntityRepository<Long, Model> {

    /**
     * Gets a user from the table that matches all the given criteria.
     *
     * @param id The id to filter for
     * @param username The username to filter for
     * @param email The email to filter for
     * @return The entity that matched the criteria, or null if no matching one was found
     */
    suspend fun getSpecific(
        id: Optional<Long> = empty(),
        username: Optional<String> = empty(),
        email: Optional<String> = empty()
    ): Model?

}

internal class ExposedUserRepository : EntityRepository<Long, Model, Entity, Table, Entity.Companion>(Table, Entity),
    UserRepository {

    override fun populateEntity(entity: Entity, model: Model, exists: Boolean): Entity = entity.apply {
        val profileEntity = model.profile?.id?.let(UserProfile.Entity::findById)
        populate(model, profileEntity)
    }

    override fun map(entity: Entity): Model = entity.asModel

    override suspend fun getSpecific(id: Optional<Long>, username: Optional<String>, email: Optional<String>): Model? =
        suspendTransaction {
            Entity.find {
                (Table.id eqOpt id) and
                        (Table.username eqOpt username) and
                        (Table.email eqOpt email)
            }
                .limit(1)
                .map(::map)
                .singleOrNull()
        }
}