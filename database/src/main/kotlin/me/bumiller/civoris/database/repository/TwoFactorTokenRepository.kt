package me.bumiller.civoris.database.repository

import kotlinx.datetime.Clock
import me.bumiller.civoris.common.Optional
import me.bumiller.civoris.common.empty
import me.bumiller.civoris.database.base.EntityRepository
import me.bumiller.civoris.database.base.IEntityRepository
import me.bumiller.civoris.database.table.TwoFactorToken
import me.bumiller.civoris.database.table.TwoFactorToken.Entity
import me.bumiller.civoris.database.table.TwoFactorToken.Model
import me.bumiller.civoris.database.table.TwoFactorToken.Table
import me.bumiller.civoris.database.table.User
import me.bumiller.civoris.database.util.eqOpt
import me.bumiller.civoris.database.util.suspendTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere

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
        token: Optional<String> = empty()
    ): Model?

    /**
     * Deletes all expired tokens in the database.
     */
    suspend fun deleteExpired()

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

    override suspend fun getSpecific(id: Optional<Long>, token: Optional<String>): Model? = suspendTransaction {
        Entity.find {
            (Table.token eqOpt token) and
                    (Table.id eqOpt id)
        }
            .limit(1)
            .map { it.asModel }
            .singleOrNull()
    }

    override suspend fun deleteExpired() {
        val now = Clock.System.now()
        table.deleteWhere {
            (Table.expiringAt.isNotNull()) and
                    (Table.expiringAt less now)
        }
    }
}