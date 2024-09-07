package me.bumiller.mol.rest.startup

import org.jetbrains.exposed.sql.Database

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
}