package me.bumiller.mol.rest.startup

import me.bumiller.mol.database.table.*
import me.bumiller.mol.database.table.crossref.LawBookMembersCrossref
import me.bumiller.mol.model.config.AppConfig
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Initializes the database connection.
 *
 * TODO: This is not good. Database should be initialized in the :database module, and the connection parameters should be moved into a config file.
 */
internal fun initDatabase(appConfig: AppConfig) {
    Database.connect(
        url = appConfig.databaseUrl,
        user = appConfig.databaseUser,
        password = appConfig.databasePassword
    )

    transaction {
        SchemaUtils.createMissingTablesAndColumns(
            User.Table,
            UserProfile.Table,
            TwoFactorToken.Table,
            LawBook.Table,
            LawEntry.Table,
            LawSection.Table,
            LawBookMembersCrossref,
            inBatch = true
        )
    }
}