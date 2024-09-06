package me.bumiller.mol.database.table

import me.bumiller.mol.database.test.util.inMemoryDatabase
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserTest {

    lateinit var db: Database

    @BeforeAll
    fun setup() {
        db = inMemoryDatabase()
    }

    @Test
    fun `Entity is mapped properly to a model`() = transaction(db) {
        val entity = User.Entity.new(1L) {
            email = "email"
            username = "username"
            password = "password"
            isEmailVerified = true
        }

        val model = entity.asModel

        assertEquals("email", model.email)
        assertEquals("username", model.username)
        assertEquals("password", model.password)
        assertEquals(true, model.isEmailVerified)
    }

    @Test
    fun `Model properly populates an entity`() = transaction {
        val userModel = User.Model(2L, "email1", "username2", "password", true, null)

        val userEntity = User.Entity.new(2L) {
            email = ""
            username = ""
            password = ""
            isEmailVerified = false
        }

        userEntity.populate(userModel, null)

        assertEquals("email1", userEntity.email)
        assertEquals("username2", userEntity.username)
        assertEquals("password", userEntity.password)
        assertEquals(true, userEntity.isEmailVerified)
        assertEquals(null, userEntity.profile)
    }

}