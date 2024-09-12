package me.bumiller.mol.database.repository

import me.bumiller.mol.common.Optional
import me.bumiller.mol.common.empty
import me.bumiller.mol.database.base.EntityRepository
import me.bumiller.mol.database.base.IEntityRepository
import me.bumiller.mol.database.table.TwoFactorToken.Entity
import me.bumiller.mol.database.table.TwoFactorToken.Model
import me.bumiller.mol.database.table.TwoFactorToken.Table
import me.bumiller.mol.database.table.User
import me.bumiller.mol.database.util.eqOpt
import me.bumiller.mol.database.util.suspendTransaction
import org.jetbrains.exposed.sql.and
import java.util.*

/**
 * Interface that grants access to the two_factor_token table in the database
 */
interface TwoFactorTokenRepository : IEntityRepository<Long, Model> {

    /**
     * Gets a specific two factor token from the table matching all given criteria
     *
     * @param id The id of the entity
     * @param token The token of the entity
     * @return The entity matching all given criteria, or null
     */
    suspend fun getSpecific(
        id: Optional<Long> = empty(),
        token: Optional<UUID> = empty()
    ): Model?

}

internal class ExposedTwoFactorTokenRepository :
    EntityRepository<Long, Model, Entity, Table, Entity.Companion>(
        Table,
        Entity
    ), TwoFactorTokenRepository {

    override fun populateEntity(entity: Entity, model: Model, exists: Boolean): Entity = entity.apply {
        val userEntity = User.Entity.findById(model.user.id)!!
        populate(model, userEntity)
    }

    override fun map(entity: Entity): Model = entity.asModel

    override suspend fun getSpecific(id: Optional<Long>, token: Optional<UUID>): Model? = suspendTransaction {
        Entity.find {
            (Table.token eqOpt token) and
                    (Table.id eqOpt id)
        }
            .limit(1)
            .map(::map)
            .singleOrNull()
    }
}