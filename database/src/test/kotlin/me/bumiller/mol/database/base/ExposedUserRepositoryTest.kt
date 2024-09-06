package me.bumiller.mol.database.base

import kotlinx.coroutines.test.runTest
import me.bumiller.mol.database.repository.ExposedUserRepository
import me.bumiller.mol.database.table.User
import me.bumiller.mol.database.test.util.inMemoryDatabase
import me.bumiller.mol.database.util.suspendTransaction
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.util.*

@TestInstance(PER_CLASS)
internal class ExposedUserRepositoryTest {

    val userRepository = ExposedUserRepository()

    lateinit var db: Database

    @BeforeAll
    fun setup() {
        db = inMemoryDatabase()
    }

    val users = listOf(
        User.Model(1L, "email-1", "username-1", "password", true, null),
        User.Model(2L, "email-2", "username-2", "password", true, null),
        User.Model(3L, "email-3", "username-3", "password", true, null),
    )

    @Test
    fun `UserRepository#getSpecific returns only matching results`() = runTest {
        suspendTransaction {
            users.forEach {
                userRepository.create(it)
            }
        }

        val byId = userRepository.getSpecific(
            id = Optional.of(1L)
        )
        val byEmail = userRepository.getSpecific(
            email = Optional.of("email-2")
        )
        val byUsername = userRepository.getSpecific(
            username = Optional.of("username-3")
        )
        val byEmailAndId = userRepository.getSpecific(
            id = Optional.of(1L),
            email = Optional.of("email-1")
        )
        val byEmailAndUsername = userRepository.getSpecific(
            email = Optional.of("email-2"),
            username = Optional.of("username-2")
        )
        val byAll = userRepository.getSpecific(
            email = Optional.of("email-3"),
            username = Optional.of("username-3"),
            id = Optional.of(3L)
        )

        val byIdFalse = userRepository.getSpecific(
            id = Optional.of(5L)
        )
        val byEmailFalse = userRepository.getSpecific(
            email = Optional.of("email-7")
        )
        val byMismatch1 = userRepository.getSpecific(
            id = Optional.of(1L),
            username = Optional.of("username-2")
        )
        val byMismatch2 = userRepository.getSpecific(
            id = Optional.of(3L),
            email = Optional.of("username-1")
        )

        assertEquals(byId!!.id, 1L)
        assertEquals(byEmail!!.id, 2L)
        assertEquals(byUsername!!.id, 3L)

        assertEquals(byEmailAndId!!.id, 1L)
        assertEquals(byEmailAndUsername!!.id, 2L)
        assertEquals(byAll!!.id, 3L)

        assertNull(byIdFalse)
        assertNull(byEmailFalse)
        assertNull(byMismatch1)
        assertNull(byMismatch2)
    }

}