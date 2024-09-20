package me.bumiller.mol.database.repository

import me.bumiller.mol.common.Optional
import me.bumiller.mol.common.empty
import me.bumiller.mol.common.present
import me.bumiller.mol.database.base.EntityRepository
import me.bumiller.mol.database.base.IEntityRepository
import me.bumiller.mol.database.table.User
import me.bumiller.mol.database.table.User.Entity
import me.bumiller.mol.database.table.User.Model
import me.bumiller.mol.database.table.User.Table
import me.bumiller.mol.database.table.UserProfile
import me.bumiller.mol.database.util.eqOpt
import me.bumiller.mol.database.util.suspendTransaction
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.and

/**
 * Repository to access the records in the users table
 */
interface UserRepository : IEntityRepository<Long, Model> {

    /**
     * Creates a new [User]
     *
     * @param model The model to take the data from
     * @param profileId The id of the profile, or null
     * @return The created [User], or null if the profile was not found
     */
    suspend fun create(model: Model, profileId: Long?): Model?

    /**
     * Gets a user from the table that matches all the given criteria.
     *
     * @param id The id to filter for
     * @param username The username to filter for
     * @param email The email to filter for
     * @param onlyActive Whether to only return the user if it is active
     * @return The entity that matched the criteria, or null if no matching one was found
     */
    suspend fun getSpecific(
        id: Optional<Long> = empty(),
        username: Optional<String> = empty(),
        email: Optional<String> = empty(),
        onlyActive: Boolean = true
    ): Model?

}

internal class ExposedUserRepository : EntityRepository<Long, Model, Entity, Table, Entity.Companion>(Table, Entity),
    UserRepository {

    override suspend fun getAll(): List<Model> = suspendTransaction {
        Entity.find {
            (Table.isEmailVerified eq true) and
                    (Table.profileId.isNotNull())
        }
            .map(Entity::asModel)
    }

    override suspend fun create(model: Model, profileId: Long?): Model? = suspendTransaction {
        val profile = if (profileId == null) null
        else UserProfile.Entity.findById(profileId) ?: return@suspendTransaction null

        Entity.new {
            this.profile = profile
            populate(model)
        }.asModel
    }

    override suspend fun getSpecific(id: Long): Model? = getSpecific(id = present(id), onlyActive = true)

    override suspend fun getSpecific(
        id: Optional<Long>,
        username: Optional<String>,
        email: Optional<String>,
        onlyActive: Boolean
    ): Model? =
        suspendTransaction {
            Entity.find {
                (Table.id eqOpt id) and
                        (Table.username eqOpt username) and
                        (Table.email eqOpt email) and
                        (
                                if (!onlyActive) Op.TRUE
                                else (Table.isEmailVerified eq true) and
                                        (Table.profileId.isNotNull())
                                )
            }
                .limit(1)
                .map { it.asModel }
                .singleOrNull()
        }
}