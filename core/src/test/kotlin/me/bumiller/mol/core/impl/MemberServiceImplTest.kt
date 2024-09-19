package me.bumiller.mol.core.impl

import io.mockk.*
import kotlinx.coroutines.test.runTest
import me.bumiller.mol.core.data.LawContentService
import me.bumiller.mol.core.data.MemberContentService
import me.bumiller.mol.core.exception.ServiceException
import me.bumiller.mol.model.MemberRole
import me.bumiller.mol.test.util.lawBookModel
import me.bumiller.mol.test.util.userModel
import me.bumiller.mol.test.util.userModels
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MemberServiceImplTest {

    private lateinit var memberContentService: MemberContentService
    private lateinit var lawContentService: LawContentService

    private lateinit var memberServiceImpl: MemberServiceImpl

    @BeforeEach
    fun setup() {
        memberContentService = mockk()
        lawContentService = mockk()

        memberServiceImpl = MemberServiceImpl(memberContentService, lawContentService)
    }

    private val user = userModel(9L)

    @Test
    fun `addMemberToBook throws if user is creator`() = runTest {
        coEvery { lawContentService.getSpecificBook(any(), any(), any()) } returns lawBookModel(1L).copy(creator = user)
        coEvery { memberContentService.getMembersInBook(1L) } returns emptyList()

        assertThrows<ServiceException.CreatorTriedAddedToBook> {
            memberServiceImpl.addMemberToBook(1L, user.id)
        }
    }

    @Test
    fun `addMemberToBook throws if user is already member`() = runTest {
        coEvery { lawContentService.getSpecificBook(any(), any(), any()) } returns lawBookModel(8L)
        coEvery { memberContentService.getMembersInBook(8L) } returns (userModels(4L) + user)

        assertThrows<ServiceException.UserAlreadyMemberOfBook> {
            memberServiceImpl.addMemberToBook(8L, user.id)
        }
    }

    @Test
    fun `addMemberToBook calls addMemberToBook with correct arguments`() = runTest {
        coEvery { lawContentService.getSpecificBook(any(), any(), any()) } returns lawBookModel(8L)
        coEvery { memberContentService.getMembersInBook(any()) } returns emptyList()
        coEvery { memberContentService.addMemberToBook(any(), any()) } just runs

        memberServiceImpl.addMemberToBook(1L, 1L)

        coVerify { memberContentService.addMemberToBook(1L, 1L) }
    }

    @Test
    fun `addMemberToBook returns members`() = runTest {
        coEvery { lawContentService.getSpecificBook(any(), any(), any()) } returns lawBookModel(8L)
        coEvery { memberContentService.getMembersInBook(any()) } returns userModels(6L)
        coEvery { memberContentService.addMemberToBook(any(), any()) } just runs

        val returned = memberServiceImpl.addMemberToBook(1L, user.id)

        assertEquals(6, returned.size)
    }

    @Test
    fun `removeMemberFromBook throws if user is not member of book`() = runTest {
        coEvery { lawContentService.getSpecificBook(any(), any(), any()) } returns lawBookModel(8L)
        coEvery { memberContentService.getMembersInBook(any()) } returns userModels(6L)

        assertThrows<ServiceException.UserNotMemberOfBook> {
            memberServiceImpl.removeMemberFromBook(1L, user.id)
        }
    }

    @Test
    fun `removeMemberFromBook throws if no other user is admin`() = runTest {
        coEvery { lawContentService.getSpecificBook(any(), any(), any()) } returns lawBookModel(1L)
        coEvery { memberContentService.getMembersInBook(any()) } returns (userModels(6L) + user)
        coEvery { memberContentService.getMemberRole(any(), 1L) } answers { m ->
            when (m.invocation.args[0] as Long) {
                9L -> MemberRole.Admin
                else -> MemberRole.Read
            }
        }

        assertThrows<ServiceException.BookNoAdminLeft> {
            memberServiceImpl.removeMemberFromBook(1L, user.id)
        }
    }

    @Test
    fun `removeMemberFromBook calls removeMemberFromBook with correct arguments`() = runTest {
        coEvery { lawContentService.getSpecificBook(any(), any(), any()) } returns lawBookModel(1L)
        coEvery { memberContentService.getMembersInBook(any()) } returns (userModels(6L) + user)
        coEvery { memberContentService.getMemberRole(any(), 1L) } returns MemberRole.Admin
        coEvery { memberContentService.removeMemberFromBook(any(), any()) } just runs

        memberServiceImpl.removeMemberFromBook(1L, user.id)

        coVerify(exactly = 1) { memberContentService.removeMemberFromBook(1L, user.id) }
    }

    @Test
    fun `removeMemberFromBook returns members`() = runTest {
        coEvery { lawContentService.getSpecificBook(any(), any(), any()) } returns lawBookModel(1L)
        coEvery { memberContentService.getMembersInBook(any()) } returns (userModels(6L) + user)
        coEvery { memberContentService.getMemberRole(any(), 1L) } returns MemberRole.Admin
        coEvery { memberContentService.removeMemberFromBook(any(), any()) } just runs

        val returned = memberServiceImpl.removeMemberFromBook(1L, user.id)

        assertEquals(6 + 1, returned.size)
    }

    @Test
    fun `setMemberRole throws if no other user is admin`() = runTest {
        coEvery { lawContentService.getSpecificBook(any(), any(), any()) } returns lawBookModel(1L)
        coEvery { memberContentService.getMembersInBook(any()) } returns (userModels(6L) + user)
        coEvery { memberContentService.getMemberRole(any(), 1L) } answers { m ->
            when (m.invocation.args[0] as Long) {
                9L -> MemberRole.Admin
                else -> MemberRole.Read
            }
        }

        assertThrows<ServiceException.BookNoAdminLeft> {
            memberServiceImpl.setMemberRole(1L, user.id, MemberRole.Read)
        }
    }

}