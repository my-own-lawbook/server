package me.bumiller.mol.database.util

import me.bumiller.mol.database.table.TwoFactorToken.Table.user
import me.bumiller.mol.database.table.User
import me.bumiller.mol.database.table.User.Table
import me.bumiller.mol.database.test.util.inMemoryDatabase
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OptionalExpressionsTest {

    private lateinit var db: Database

    @BeforeAll
    fun setup() {
        db = inMemoryDatabase()
    }

    val users = (1..10).map {
        User.Model(it.toLong(), "email$it", "username$it", "password$it", it % 2 == 0, null)
    }

    @Test
    fun `eqOpt works for java Optional`() = transaction {
        users.forEach { user ->
            Table.insert {
                it[email] = user.email
                it[username] = user.username
                it[password] = user.password
                it[isEmailVerified] = user.isEmailVerified
                it[profileId] = user.profile?.id
            }
        }

        val all = Table.selectAll().where {
            Table.email eqOpt Optional.empty()
        }
        val byEmail = Table.selectAll().where {
            Table.email eqOpt Optional.of("email2")
        }
        val byMixed = Table.selectAll().where {
            (Table.email eqOpt Optional.of("email3")) and
                    (Table.username eqOpt Optional.of("username3"))
        }
        val byInvalid = Table.selectAll().where {
            Table.email eqOpt Optional.of("eksops")
        }

        assertEquals(users.size, all.count().toInt())
        assertEquals(1L, byEmail.count())
        assertEquals(1L, byMixed.count())
        assertEquals(0L, byInvalid.count())
    }

}