package me.bumiller.mol.database.repository

import me.bumiller.mol.database.base.EntityRepository
import me.bumiller.mol.database.base.IEntityRepository
import me.bumiller.mol.database.table.UserProfile
import me.bumiller.mol.database.table.UserProfile.Entity
import me.bumiller.mol.database.table.UserProfile.Model
import me.bumiller.mol.database.table.UserProfile.Table

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

    override suspend fun create(model: Model): Model = Entity.new {
        populate(model)
    }.asModel

}