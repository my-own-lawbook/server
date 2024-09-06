package me.bumiller.mol.database.test.util

import me.bumiller.mol.database.table.TwoFactorToken
import me.bumiller.mol.database.table.User
import me.bumiller.mol.database.table.UserProfile
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Creates an in memory database for testing purposes
 *
 * @return The h2 database handle
 */
fun inMemoryDatabase(): Database {
    val db = Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")

    transaction {
        SchemaUtils.drop(User.Table, UserProfile.Table, TwoFactorToken.Table, inBatch = true)

        SchemaUtils.create(User.Table)
        SchemaUtils.create(UserProfile.Table)
        SchemaUtils.create(TwoFactorToken.Table)
    }

    return db
}