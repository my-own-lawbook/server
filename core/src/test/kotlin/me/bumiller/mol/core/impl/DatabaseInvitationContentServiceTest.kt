package me.bumiller.mol.core.impl

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import me.bumiller.mol.core.data.InvitationContentService
import me.bumiller.mol.core.exception.ServiceException
import me.bumiller.mol.database.repository.BookInvitationRepository
import me.bumiller.mol.database.repository.LawBookRepository
import me.bumiller.mol.database.repository.UserRepository
import me.bumiller.mol.database.table.BookInvitation
import me.bumiller.mol.model.InvitationStatus
import me.bumiller.mol.model.MemberRole
import me.bumiller.mol.test.util.invitationEntities
import me.bumiller.mol.test.util.invitationEntity
import me.bumiller.mol.test.util.lawBookEntity
import me.bumiller.mol.test.util.userEntity
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DatabaseInvitationContentServiceTest {

    private lateinit var invitationRepository: BookInvitationRepository
    private lateinit var userRepository: UserRepository
    private lateinit var bookRepository: LawBookRepository

    private lateinit var invitationContentService: InvitationContentService

    @BeforeEach
    fun setup() {
        invitationRepository = mockk()
        userRepository = mockk()
        bookRepository = mockk()

        invitationContentService =
            DatabaseInvitationContentService(invitationRepository, userRepository, bookRepository)
    }

    @Test
    fun `getInvitationById returns invitation from repository`() = runTest {
        val entity = invitationEntity(1L)
        coEvery { invitationRepository.getSpecific(any()) } returns entity

        val returned = invitationContentService.getInvitationById(1L)

        assertEquals(returned.id, entity.id)
        assertEquals(returned.sentAt, entity.sentAt)
    }

    @Test
    fun `getInvitationById throws if invitation is not found`() = runTest {
        invitationEntity(1L)
        coEvery { invitationRepository.getSpecific(any()) } returns null

        assertThrows<ServiceException.InvitationNotFound> {
            invitationContentService.getInvitationById(1L)
        }
    }

    @Test
    fun `getAll correctly passes the arguments`() = runTest {
        coEvery { invitationRepository.getAll(any(), any(), any(), any(), any()) } returns emptyList()

        invitationContentService.getAll(1L, null, 5L, listOf(InvitationStatus.Open, InvitationStatus.Declined), true)

        coVerify {
            invitationRepository.getAll(
                1L,
                null,
                5L,
                listOf(BookInvitation.Status.Open, BookInvitation.Status.Denied),
                true
            )
        }
    }

    @Test
    fun `getAll correctly returns the invitations`() = runTest {
        coEvery { invitationRepository.getAll(any(), any(), any(), any(), any()) } returns invitationEntities(7L)

        val returned = invitationContentService.getAll()

        assertEquals(7, returned.size)
    }

    @Test
    fun `createInvitation throws if author is not found`() = runTest {
        coEvery { userRepository.getSpecific(1L) } returns null

        assertThrows<ServiceException.UserNotFound> {
            invitationContentService.createInvitation(1L, 1L, 5L)
        }
    }

    @Test
    fun `createInvitation throws if book is not found`() = runTest {
        coEvery { userRepository.getSpecific(1L) } returns userEntity(1L)
        coEvery { bookRepository.getSpecific(1L) } returns null

        assertThrows<ServiceException.LawBookNotFound> {
            invitationContentService.createInvitation(1L, 1L, 5L)
        }
    }

    @Test
    fun `createInvitation throws if recipient is not found`() = runTest {
        coEvery { userRepository.getSpecific(1L) } returns userEntity(1L)
        coEvery { bookRepository.getSpecific(1L) } returns lawBookEntity(1L)
        coEvery { userRepository.getSpecific(5L) } returns null

        assertThrows<ServiceException.UserNotFound> {
            invitationContentService.createInvitation(1L, 1L, 5L)
        }
    }

    @Test
    fun `createInvitation calls create with correct arguments`() = runTest {
        coEvery { userRepository.getSpecific(1L) } returns userEntity(1L)
        coEvery { bookRepository.getSpecific(1L) } returns lawBookEntity(1L)
        coEvery { userRepository.getSpecific(5L) } returns userEntity(5L)

        val invitationSlot = slot<BookInvitation.Model>()
        coEvery { invitationRepository.create(capture(invitationSlot), any(), any(), any()) } returns invitationEntity(
            1L
        )

        val instant = Clock.System.now()

        invitationContentService.createInvitation(1L, 1L, 5L, MemberRole.Moderator, instant, "message-123")

        invitationSlot.captured.run {
            assertEquals("message-123", message)
            assertEquals(instant, expiresAt)
            assertNull(usedAt)
            assertEquals(BookInvitation.Status.Open, status)
        }
        coVerify { invitationRepository.create(any(), 1L, 1L, 5L) }
    }

    @Test
    fun `createInvitation returns created invitation`() = runTest {
        coEvery { userRepository.getSpecific(1L) } returns userEntity(1L)
        coEvery { bookRepository.getSpecific(1L) } returns lawBookEntity(1L)
        coEvery { userRepository.getSpecific(5L) } returns userEntity(5L)

        coEvery { invitationRepository.create(any(), any(), any(), any()) } returns invitationEntity(145L)

        val returned =
            invitationContentService.createInvitation(
                1L,
                1L,
                5L,
                MemberRole.Moderator,
                Clock.System.now(),
                "message-123"
            )

        assertEquals(145L, returned.id)
    }

    @Test
    fun `updateStatus throws if invitation is not found`() = runTest {
        coEvery { invitationRepository.getSpecific(any()) } returns null

        assertThrows<ServiceException.InvitationNotFound> {
            invitationContentService.updateStatus(1L, InvitationStatus.Declined)
        }
    }

    @Test
    fun `updateStatus updates correct attributes of model`() = runTest {
        val invitation = invitationEntity(1L)
        coEvery { invitationRepository.getSpecific(any()) } returns invitation

        val invitationSlot = slot<BookInvitation.Model>()
        coEvery { invitationRepository.update(capture(invitationSlot)) } returns invitation

        invitationContentService.updateStatus(1L, InvitationStatus.Revoked)

        assertEquals(BookInvitation.Status.Revoked, invitationSlot.captured.status)
        assertNotNull(invitationSlot.captured.usedAt)
    }

}