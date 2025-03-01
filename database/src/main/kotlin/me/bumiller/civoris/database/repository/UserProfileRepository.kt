package me.bumiller.civoris.database.repository

import me.bumiller.civoris.database.base.EntityRepository
import me.bumiller.civoris.database.base.IEntityRepository
import me.bumiller.civoris.database.table.UserProfile
import me.bumiller.civoris.database.table.UserProfile.Entity
import me.bumiller.civoris.database.table.UserProfile.Model
import me.bumiller.civoris.database.table.UserProfile.Table
import me.bumiller.civoris.database.util.suspendTransaction

/**
 * Repository to access the records inside the user_profile table
 */
interface UserProfileRepository : IEntityRepository<Long, Model> {

    /**
     * Creates a new [UserProfile]
     *
     * @param model The model to take the data from
     * @return The created [UserProfile]
     */
    suspend fun create(model: Model): Model

}

internal class ExposedUserProfileRepository :
    EntityRepository<Long, Model, Entity, Table, Entity.Companion>(Table, Entity), UserProfileRepository {

    override suspend fun create(model: Model): Model = suspendTransaction {
        Entity.new {
            populate(model)
        }.asModel
    }

}