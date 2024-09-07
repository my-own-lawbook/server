package me.bumiller.mol.rest.startup

import me.bumiller.mol.database.table.TwoFactorToken
import me.bumiller.mol.database.table.User
import me.bumiller.mol.database.table.UserProfile
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Initializes the database connection.
 *
 * TODO: This is not good. Database should be initialized in the :database module, and the connection parameters should be moved into a config file.
 */
internal fun initDatabase() {
    Database.connect(
        url = "jdbc:postgresql://localhost:5432/mol_db",
        user = "admin",
        password = "admin"
    )

    transaction {
        SchemaUtils.createMissingTablesAndColumns(User.Table, UserProfile.Table, TwoFactorToken.Table, inBatch = true)
    }
}