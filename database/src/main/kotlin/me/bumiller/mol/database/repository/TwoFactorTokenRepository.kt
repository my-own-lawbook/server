package me.bumiller.mol.database.repository

import me.bumiller.mol.common.Optional
import me.bumiller.mol.common.empty
import me.bumiller.mol.database.base.EntityRepository
import me.bumiller.mol.database.base.IEntityRepository
import me.bumiller.mol.database.table.TwoFactorToken
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
     * Creates a new [TwoFactorToken]
     *
     * @param model The model to take the data from
     * @param userId The id of the user
     * @return The created model, or null if the user was not found
     */
    suspend fun create(model: Model, userId: Long): Model?

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

    override suspend fun create(model: Model, userId: Long): Model? = suspendTransaction {
        val user = User.Entity.findById(userId) ?: return@suspendTransaction null

        Entity.new {
            this.user = user
            populate(model)
        }.asModel
    }

    override suspend fun getSpecific(id: Optional<Long>, token: Optional<UUID>): Model? = suspendTransaction {
        Entity.find {
            (Table.token eqOpt token) and
                    (Table.id eqOpt id)
        }
            .limit(1)
            .map { it.asModel }
            .singleOrNull()
    }
}