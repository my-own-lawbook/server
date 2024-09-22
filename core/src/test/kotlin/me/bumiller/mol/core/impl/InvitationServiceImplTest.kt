package me.bumiller.mol.core.impl

import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import me.bumiller.mol.core.InvitationService
import me.bumiller.mol.core.exception.ServiceException
import me.bumiller.mol.model.InvitationStatus
import me.bumiller.mol.model.MemberRole
import me.bumiller.mol.test.util.invitationModel
import me.bumiller.mol.test.util.invitationModels
import me.bumiller.mol.test.util.userModels
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.time.Duration.Companion.minutes

class InvitationServiceImplTest {

    private lateinit var invitationContentService: DatabaseInvitationContentService
    private lateinit var memberContentService: DatabaseMemberContentService

    private lateinit var invitationService: InvitationService

    @BeforeEach
    fun setup() {
        invitationContentService = mockk()
        memberContentService = mockk()

        invitationService = InvitationServiceImpl(invitationContentService, memberContentService)
    }

    @Test
    fun `createInvitation throws if recipient is already member of the book`() = runTest {
        coEvery { memberContentService.getMemberRole(1L, 1L) } returns MemberRole.Admin
        coEvery { memberContentService.getMembersInBook(1L) } returns userModels(4L)

        assertThrows<ServiceException.UserAlreadyMemberOfBook> {
            invitationService.createInvitation(1L, 1L, 1L)
        }
    }

    @Test
    fun `createInvitation throws if an invitation for recipient and book already exists`() = runTest {
        coEvery { memberContentService.getMemberRole(1L, 1L) } returns MemberRole.Admin
        coEvery { memberContentService.getMembersInBook(1L) } returns userModels(4L).filterNot { it.id == 1L }
        coEvery { invitationContentService.getAll(any(), any(), any(), any(), any()) } returns invitationModels(3)

        assertThrows<ServiceException.OpenInvitationAlreadyPresent> {
            invitationService.createInvitation(1L, 1L, 1L)
        }

        coVerify { invitationContentService.getAll(null, 1L, 1L, listOf(InvitationStatus.Open), true) }
    }

    @Test
    fun `createInvitation returns invitation`() = runTest {
        coEvery { memberContentService.getMemberRole(1L, 1L) } returns MemberRole.Admin
        coEvery { memberContentService.getMembersInBook(1L) } returns userModels(4L).filterNot { it.id == 1L }
        coEvery { invitationContentService.getAll(any(), any(), any(), any(), any()) } returns emptyList()
        coEvery {
            invitationContentService.createInvitation(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns invitationModel(7L)

        val returned = invitationService.createInvitation(1L, 1L, 1L)
        assertEquals(7L, returned.id)
    }

    @Test
    fun `acceptInvitation throws if the invitation is not open`() = runTest {
        coEvery { invitationContentService.getInvitationById(1L) } returns invitationModel(3L).copy(status = InvitationStatus.Revoked)

        assertThrows<ServiceException.InvitationNotOpen> {
            invitationService.acceptInvitation(1L)
        }
    }

    @Test
    fun `acceptInvitation throws if the invitation is expired`() = runTest {
        val expired = Clock.System.now().minus(5.minutes)
        coEvery { invitationContentService.getInvitationById(1L) } returns invitationModel(3L).copy(
            status = InvitationStatus.Open,
            expiredAt = expired
        )

        assertThrows<ServiceException.InvitationExpired> {
            invitationService.acceptInvitation(1L)
        }
    }

    @Test
    fun `acceptInvitation marks invitation as accepted`() = runTest {
        coEvery { invitationContentService.getInvitationById(1L) } returns invitationModel(3L).copy(
            status = InvitationStatus.Open,
            expiredAt = null
        )
        coEvery { memberContentService.addMemberToBook(any(), any()) } just runs
        coEvery { memberContentService.setMemberRole(any(), any(), any()) } just runs
        coEvery { invitationContentService.updateStatus(any(), any()) } just runs

        invitationService.acceptInvitation(1L)

        coVerify { invitationContentService.updateStatus(1L, InvitationStatus.Accepted) }
    }

    @Test
    fun `acceptInvitation adds the user to the book`() = runTest {
        val invitation = invitationModel(3L).copy(status = InvitationStatus.Open, expiredAt = null)
        coEvery { invitationContentService.getInvitationById(1L) } returns invitation
        coEvery { memberContentService.addMemberToBook(any(), any()) } just runs
        coEvery { memberContentService.setMemberRole(any(), any(), any()) } just runs
        coEvery { invitationContentService.updateStatus(any(), any()) } just runs

        invitationService.acceptInvitation(1L)

        coVerify { memberContentService.addMemberToBook(invitation.targetBook.id, invitation.recipient.id) }
    }

    @Test
    fun `acceptInvitation sets the new members role`() = runTest {
        val invitation = invitationModel(3L).copy(status = InvitationStatus.Open, expiredAt = null)
        coEvery { invitationContentService.getInvitationById(1L) } returns invitation
        coEvery { memberContentService.addMemberToBook(any(), any()) } just runs
        coEvery { memberContentService.setMemberRole(any(), any(), any()) } just runs
        coEvery { invitationContentService.updateStatus(any(), any()) } just runs

        invitationService.acceptInvitation(1L)

        coVerify {
            memberContentService.setMemberRole(
                invitation.recipient.id,
                invitation.targetBook.id,
                invitation.role
            )
        }
    }

    @Test
    fun `denyInvitation throws if the invitation is not open`() = runTest {
        coEvery { invitationContentService.getInvitationById(1L) } returns invitationModel(3L).copy(status = InvitationStatus.Revoked)

        assertThrows<ServiceException.InvitationNotOpen> {
            invitationService.denyInvitation(1L)
        }
    }

    @Test
    fun `denyInvitation marks invitation as denied`() = runTest {
        coEvery { invitationContentService.getInvitationById(1L) } returns invitationModel(3L).copy(
            status = InvitationStatus.Open,
            expiredAt = null
        )
        coEvery { invitationContentService.updateStatus(any(), any()) } just runs

        invitationService.denyInvitation(1L)

        coVerify { invitationContentService.updateStatus(1L, InvitationStatus.Declined) }
    }

    @Test
    fun `revokeInvitation throws if the invitation is not open`() = runTest {
        coEvery { invitationContentService.getInvitationById(1L) } returns invitationModel(3L).copy(status = InvitationStatus.Revoked)

        assertThrows<ServiceException.InvitationNotOpen> {
            invitationService.revokeInvitation(1L)
        }
    }

    @Test
    fun `revokeInvitation marks invitation as revoked`() = runTest {
        coEvery { invitationContentService.getInvitationById(1L) } returns invitationModel(3L).copy(
            status = InvitationStatus.Open,
            expiredAt = null
        )
        coEvery { invitationContentService.updateStatus(any(), any()) } just runs

        invitationService.revokeInvitation(1L)

        coVerify { invitationContentService.updateStatus(1L, InvitationStatus.Revoked) }
    }

}