package me.bumiller.mol.core.impl

import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.bumiller.mol.common.Optional
import me.bumiller.mol.common.empty
import me.bumiller.mol.common.present
import me.bumiller.mol.core.data.TwoFactorTokenService
import me.bumiller.mol.database.repository.TwoFactorTokenRepository
import me.bumiller.mol.database.repository.UserRepository
import me.bumiller.mol.database.table.TwoFactorToken
import me.bumiller.mol.database.table.User
import me.bumiller.mol.model.TwoFactorTokenType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import me.bumiller.mol.database.table.TwoFactorToken.Model as TwoFactorTokenModel

class DatabaseTwoFactorTokenServiceTest {

    lateinit var mockTokenRepository: TwoFactorTokenRepository
    lateinit var mockUserRepository: UserRepository

    lateinit var tokenService: TwoFactorTokenService

    @BeforeEach
    fun setup() {
        mockTokenRepository = mockk()
        mockUserRepository = mockk()

        tokenService = DatabaseTwoFactorTokenService(mockTokenRepository, mockUserRepository)
    }

    val userModel = User.Model(1L, "email", "username", "password", true, null)

    val tokens = (1..10).map {
        TwoFactorTokenModel(
            it.toLong(),
            UUID.randomUUID(),
            issuedAt = Instant.fromEpochMilliseconds(it.toLong()),
            expiringAt = Instant.fromEpochMilliseconds(it.toLong()),
            used = it % 2 == 0,
            additionalContent = "content-$it",
            type = if (it % 2 == 0) TwoFactorTokenType.RefreshToken.serializedName else TwoFactorTokenType.EmailConfirm.serializedName,
            user = userModel
        )
    }

    @Test
    fun `getAll returns all tokens`() = runTest {
        coEvery { mockTokenRepository.getAll() } returns tokens

        val returned = tokenService.getAll()

        assertEquals(tokens.size, returned.size)
        assertArrayEquals((1L..10L).toList().toTypedArray(), returned.map { it.id }.sorted().toTypedArray())
    }

    @Test
    fun `getSpecific correctly maps arguments to Optional`() = runTest {
        val idSlot = slot<Optional<Long>>()
        val tokenSlot = slot<Optional<UUID>>()

        val uuid = UUID.randomUUID()

        coEvery { mockTokenRepository.getSpecific(capture(idSlot), capture(tokenSlot)) } returns tokens.first()

        tokenService.getSpecific(null, uuid)
        assertEquals(empty<Long>(), idSlot.captured)
        assertEquals(present(uuid), tokenSlot.captured)

        tokenService.getSpecific()
        assertEquals(empty<Long>(), idSlot.captured)
        assertEquals(empty<UUID>(), tokenSlot.captured)

        tokenService.getSpecific(1L, uuid)
        assertEquals(present(1L), idSlot.captured)
        assertEquals(present(uuid), tokenSlot.captured)
    }

    @Test
    fun `create correctly maps arguments to created token`() = runTest {
        coEvery {
            mockTokenRepository.create(
                any(),
                any()
            )
        } answers { c -> c.invocation.args[0] as TwoFactorTokenModel }
        coEvery { mockUserRepository.getSpecific(any<Long>()) } returns userModel

        val time = Clock.System.now()

        val returned1 = tokenService.create(TwoFactorTokenType.RefreshToken, userModel.id, time, time, "content")
        val returned2 = tokenService.create(TwoFactorTokenType.EmailConfirm, userModel.id)

        assertEquals("content", returned1?.additionalInfo)
        assertEquals(time, returned1?.expiringAt)
        assertEquals(time, returned1?.issuedAt)
        assertEquals(TwoFactorTokenType.RefreshToken, returned1?.type)

        assertEquals(TwoFactorTokenType.EmailConfirm, returned2?.type)
    }

    @Test
    fun `create returns null when user is not found`() = runTest {
        coEvery { mockUserRepository.getSpecific(any<Long>()) } returns null

        val time = Clock.System.now()

        val result = tokenService.create(TwoFactorTokenType.RefreshToken, userModel.id, time, time, "content")

        assertNull(result)
    }

    @Test
    fun `markUsed correctly updated used to true`() = runTest {
        coEvery { mockTokenRepository.getSpecific(any<Long>()) } returns tokens.first()
        coEvery { mockTokenRepository.update(any()) } answers { c -> c.invocation.args[0] as TwoFactorToken.Model }

        val time = Clock.System.now()

        val token = me.bumiller.mol.model.TwoFactorToken(
            1L,
            UUID.randomUUID(),
            null,
            time,
            time,
            TwoFactorTokenType.RefreshToken,
            true,
            mockk()
        )

        val updated = tokenService.markAsUsed(token.id)

        assertTrue(updated!!.used)
    }

    @Test
    fun `markUsed returns null when token is not found`() = runTest {
        coEvery { mockTokenRepository.getSpecific(any<Long>()) } returns null

        val time = Clock.System.now()

        val token = me.bumiller.mol.model.TwoFactorToken(
            1L,
            UUID.randomUUID(),
            null,
            time,
            time,
            TwoFactorTokenType.RefreshToken,
            true,
            mockk()
        )

        val updated = tokenService.markAsUsed(token.id)

        assertNull(updated)
    }

}