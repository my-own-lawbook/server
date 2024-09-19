package me.bumiller.mol.core.impl

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import me.bumiller.mol.core.data.MemberContentService
import me.bumiller.mol.core.exception.ServiceException
import me.bumiller.mol.database.repository.LawBookRepository
import me.bumiller.mol.database.repository.MemberRoleRepository
import me.bumiller.mol.database.repository.UserRepository
import me.bumiller.mol.database.table.LawBook
import me.bumiller.mol.model.MemberRole
import me.bumiller.mol.model.User
import me.bumiller.mol.test.util.lawBookEntity
import me.bumiller.mol.test.util.userEntities
import me.bumiller.mol.test.util.userEntity
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DatabaseMemberContentServiceTest {

    private lateinit var bookRepository: LawBookRepository
    private lateinit var userRepository: UserRepository
    private lateinit var roleRepository: MemberRoleRepository

    private lateinit var memberContentService: MemberContentService

    @BeforeEach
    fun setup() {
        bookRepository = mockk()
        userRepository = mockk()
        roleRepository = mockk()

        memberContentService = DatabaseMemberContentService(bookRepository, userRepository, roleRepository)
    }

    @Test
    fun `getMembersInBook throws if no book found`() = runTest {
        coEvery { bookRepository.getSpecific(1L) } returns null

        assertThrows<ServiceException.LawBookNotFound> {
            memberContentService.getMembersInBook(1L)
        }
    }

    @Test
    fun `getMembersInBook returns all members from database`() = runTest {
        val userEntities = userEntities(10L)
        val bookEntity = lawBookEntity(1L).copy(members = userEntities)

        coEvery { bookRepository.getSpecific(bookEntity.id) } returns bookEntity

        val returned = memberContentService.getMembersInBook(bookEntity.id)

        assertArrayEquals((1L..10L).toList().toTypedArray(), returned.map(User::id).sorted().toTypedArray())
    }

    @Test
    fun `addMemberToBook throws if either book or user was not found`() = runTest {
        coEvery { bookRepository.getSpecific(1L) } returns null

        assertThrows<ServiceException.LawBookNotFound> {
            memberContentService.addMemberToBook(1L, 1L)
        }

        coEvery { bookRepository.getSpecific(1L) } returns lawBookEntity(1L)
        coEvery { userRepository.getSpecific(1L) } returns null

        assertThrows<ServiceException.UserNotFound> {
            memberContentService.addMemberToBook(1L, 1L)
        }
    }

    @Test
    fun `addMemberToBook only calls update with correctly updated member field`() = runTest {
        val members = userEntities(3L)
        val user = userEntity(4L)
        val book = lawBookEntity(1L).copy(members = members)

        val bookSlot = slot<LawBook.Model>()

        coEvery { bookRepository.getSpecific(book.id) } returns book
        coEvery { userRepository.getSpecific(user.id) } returns user
        coEvery { bookRepository.update(capture(bookSlot)) } returns book

        memberContentService.addMemberToBook(book.id, user.id)

        assertArrayEquals(
            arrayOf(1L, 2L, 3L, 4L),
            bookSlot.captured.members.map(me.bumiller.mol.database.table.User.Model::id).sorted().toTypedArray()
        )
    }

    @Test
    fun `removeMemberFromBook throws if either book or user was not found`() = runTest {
        coEvery { bookRepository.getSpecific(1L) } returns null

        assertThrows<ServiceException.LawBookNotFound> {
            memberContentService.addMemberToBook(1L, 1L)
        }

        coEvery { bookRepository.getSpecific(1L) } returns lawBookEntity(1L)
        coEvery { userRepository.getSpecific(1L) } returns null

        assertThrows<ServiceException.UserNotFound> {
            memberContentService.removeMemberFromBook(1L, 1L)
        }
    }

    @Test
    fun `removeMemberFromBook only calls update with correctly updated member field`() = runTest {
        val members = userEntities(3L)
        val user = userEntity(2L)
        val book = lawBookEntity(1L).copy(members = members)

        val bookSlot = slot<LawBook.Model>()

        coEvery { bookRepository.getSpecific(book.id) } returns book
        coEvery { userRepository.getSpecific(user.id) } returns user
        coEvery { bookRepository.update(capture(bookSlot)) } returns book

        memberContentService.removeMemberFromBook(book.id, user.id)

        assertArrayEquals(
            arrayOf(1L, 3L),
            bookSlot.captured.members.map(me.bumiller.mol.database.table.User.Model::id).sorted().toTypedArray()
        )
    }

    @Test
    fun `setMemberRole calls setMemberRole with correct args and returns true`() = runTest {
        coEvery { userRepository.getSpecific(1L) } returns userEntity(1L)
        coEvery { bookRepository.getSpecific(1L) } returns lawBookEntity(1L).copy(members = userEntities(4L))
        coEvery { roleRepository.setMemberRole(any(), any(), any()) } returns Unit

        coEvery { roleRepository.getMemberRole(any(), 1L) } answers { m ->
            when (m.invocation.args[0] as Long) {
                1L -> "admin"
                2L -> "admin"
                3L -> "write"
                4L -> "update"
                else -> error(Unit)
            }
        }

        memberContentService.setMemberRole(1L, 1L, MemberRole.Read)

        coVerify(exactly = 1) { roleRepository.setMemberRole(1L, 1L, "read") }
    }

}