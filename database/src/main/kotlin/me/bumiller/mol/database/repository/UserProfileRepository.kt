package me.bumiller.mol.database.repository

import me.bumiller.mol.database.base.EntityRepository
import me.bumiller.mol.database.base.IEntityRepository
import me.bumiller.mol.database.table.UserProfile.Entity
import me.bumiller.mol.database.table.UserProfile.EntityClass
import me.bumiller.mol.database.table.UserProfile.Model
import me.bumiller.mol.database.table.UserProfile.Table

/**
 * Repository to access the records inside the user_profile table
 */
interface UserProfileRepository : IEntityRepository<Long, Model>

internal class ExposedUserProfileRepository :
    EntityRepository<Long, Model, Entity, Table, EntityClass>(Table, EntityClass), UserProfileRepository {

    override fun populateEntity(entity: Entity, model: Model): Entity = entity.apply {
        populate(model)
    }

    override fun map(entity: Entity): Model = entity.asModel
}