package me.bumiller.mol.database.base

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import me.bumiller.mol.common.present
import me.bumiller.mol.database.repository.ExposedTwoFactorTokenRepository
import me.bumiller.mol.database.repository.ExposedUserRepository
import me.bumiller.mol.database.table.TwoFactorToken
import me.bumiller.mol.database.table.User
import me.bumiller.mol.database.test.util.inMemoryDatabase
import me.bumiller.mol.database.util.suspendTransaction
import org.jetbrains.exposed.sql.Database
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.util.*

@TestInstance(PER_CLASS)
internal class ExposedTwoFactorTokenRepositoryTest {

    val tokenRepository = ExposedTwoFactorTokenRepository()
    val userRepository = ExposedUserRepository()

    lateinit var db: Database

    @BeforeAll
    fun setup() {
        db = inMemoryDatabase()
    }

    val user = User.Model(1L, "email", "username", "password", true, null)

    val tokens = listOf(
        TwoFactorToken.Model(
            1L,
            UUID.fromString("65ae72f3-6cff-4527-8353-0eba80e0b3d8"),
            Clock.System.now(),
            Clock.System.now(),
            true,
            null,
            "",
            user
        ),
        TwoFactorToken.Model(
            2L,
            UUID.fromString("c8744bcb-1cc9-4b26-be8b-5d6f591ceaa5"),
            Clock.System.now(),
            Clock.System.now(),
            true,
            null,
            "",
            user
        ),
        TwoFactorToken.Model(
            3L,
            UUID.fromString("653032ed-aea9-4817-802c-2030d6904aef"),
            Clock.System.now(),
            Clock.System.now(),
            true,
            null,
            "",
            user
        ),
    )

    @Test
    fun `getSpecific returns only matching results`() = runTest {
        suspendTransaction {
            userRepository.create(user)
            tokens.forEach {
                tokenRepository.create(it)
            }
        }

        val byId = tokenRepository.getSpecific(
            id = present(1L)
        )
        val byToken = tokenRepository.getSpecific(
            token = present(UUID.fromString("653032ed-aea9-4817-802c-2030d6904aef"))
        )
        val byAll = tokenRepository.getSpecific(
            id = present(2L),
            token = present(UUID.fromString("c8744bcb-1cc9-4b26-be8b-5d6f591ceaa5"))
        )

        val byIdFalse = tokenRepository.getSpecific(
            id = present(5L)
        )
        val byTokenFalse = tokenRepository.getSpecific(
            token = present(UUID.randomUUID())
        )
        val byMismatch = tokenRepository.getSpecific(
            id = present(1L),
            token = present(UUID.fromString("653032ed-aea9-4817-802c-2030d6904aef"))
        )

        assertEquals(byId!!.id, 1L)
        assertEquals(byToken!!.id, 3L)
        assertEquals(byAll!!.id, 2L)

        assertNull(byIdFalse)
        assertNull(byTokenFalse)
        assertNull(byMismatch)
    }

}