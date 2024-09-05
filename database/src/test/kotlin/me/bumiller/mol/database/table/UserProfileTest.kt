package me.bumiller.mol.database.table

import kotlinx.datetime.LocalDate
import me.bumiller.mol.database.test.util.inMemoryDatabase
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserProfileTest {

    lateinit var db: Database

    @BeforeAll
    fun setup() {
        db = inMemoryDatabase()

        transaction {
            SchemaUtils.create(User.Table)
            SchemaUtils.create(UserProfile.Table)
            SchemaUtils.create(TwoFactorToken.Table)
        }
    }

    @Test
    fun `Entity is mapped properly to a model`() = transaction {
        val entity = UserProfile.Entity.new {
            firstName = "firstName"
            lastName = "lastName"
            birthday = LocalDate(2000, 1, 1)
            gender = "gender"
        }

        val model = entity.asModel

        assertEquals("firstName", model.firstName)
        assertEquals("lastName", model.lastName)
        assertEquals("gender", model.gender)
        assertEquals(LocalDate(2000, 1, 1), model.birthday)
    }

    @Test
    fun `Model properly populates an entity`() = transaction {
        val model = UserProfile.Model(1L, LocalDate(2000, 1, 1), "firstName", "lastName", "gender")

        val entity = UserProfile.Entity.new {
            firstName = ""
            lastName = ""
            birthday = LocalDate(1900, 1, 1)
            gender = ""
        }

        entity.populate(model)

        assertEquals("firstName", entity.firstName)
        assertEquals("lastName", entity.lastName)
        assertEquals("gender", entity.gender)
        assertEquals(LocalDate(2000, 1, 1), entity.birthday)
    }

}