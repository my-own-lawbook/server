package me.bumiller.mol.database.test.util

import org.jetbrains.exposed.sql.Database

/**
 * Creates an in memory database for testing purposes
 *
 * @return The h2 database handle
 */
fun inMemoryDatabase(): Database = Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")