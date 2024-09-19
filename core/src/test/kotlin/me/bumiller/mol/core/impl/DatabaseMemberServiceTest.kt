package me.bumiller.mol.core.impl

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import me.bumiller.mol.core.data.MemberService
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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DatabaseMemberServiceTest {

    private lateinit var bookRepository: LawBookRepository
    private lateinit var userRepository: UserRepository
    private lateinit var roleRepository: MemberRoleRepository

    private lateinit var memberService: MemberService

    @BeforeEach
    fun setup() {
        bookRepository = mockk()
        userRepository = mockk()
        roleRepository = mockk()

        memberService = DatabaseMemberService(bookRepository, userRepository, roleRepository)
    }

    @Test
    fun `getMembersInBook throws if no book found`() = runTest {
        coEvery { bookRepository.getSpecific(1L) } returns null

        assertThrows<ServiceException.LawBookNotFound> {
            memberService.getMembersInBook(1L)
        }
    }

    @Test
    fun `getMembersInBook returns all members from database`() = runTest {
        val userEntities = userEntities(10L)
        val bookEntity = lawBookEntity(1L).copy(members = userEntities)

        coEvery { bookRepository.getSpecific(bookEntity.id) } returns bookEntity

        val returned = memberService.getMembersInBook(bookEntity.id)

        assertArrayEquals((1L..10L).toList().toTypedArray(), returned.map(User::id).sorted().toTypedArray())
    }

    @Test
    fun `addMemberToBook throws if either book or user was not found`() = runTest {
        coEvery { bookRepository.getSpecific(1L) } returns null

        assertThrows<ServiceException.LawBookNotFound> {
            memberService.addMemberToBook(1L, 1L)
        }

        coEvery { bookRepository.getSpecific(1L) } returns lawBookEntity(1L)
        coEvery { userRepository.getSpecific(1L) } returns null

        assertThrows<ServiceException.UserNotFound> {
            memberService.addMemberToBook(1L, 1L)
        }
    }

    @Test
    fun `addMemberToBook throws if user is already member of the book`() = runTest {
        val members = userEntities(3L)
        val user1 = userEntity(3L)
        val user2 = userEntity(4L)
        val book = lawBookEntity(1L).copy(members = members)

        coEvery { bookRepository.getSpecific(book.id) } returns book
        coEvery { userRepository.getSpecific(user1.id) } returns user1
        coEvery { bookRepository.update(any()) } returns book
        coEvery { userRepository.getSpecific(user2.id) } returns user2

        assertThrows<ServiceException.UserAlreadyMemberOfBook> {
            memberService.addMemberToBook(book.id, user1.id)
        }

        memberService.addMemberToBook(book.id, user2.id)
        coVerify(exactly = 1) { bookRepository.update(any()) }
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

        memberService.addMemberToBook(book.id, user.id)

        assertArrayEquals(
            arrayOf(1L, 2L, 3L, 4L),
            bookSlot.captured.members.map(me.bumiller.mol.database.table.User.Model::id).sorted().toTypedArray()
        )
    }

    @Test
    fun `addMemberToBook returns the updated members`() = runTest {
        val members = userEntities(3L)
        val user = userEntity(4L)
        val book1 = lawBookEntity(1L).copy(members = members)
        val book2 = lawBookEntity(2L).copy(members = userEntities(6L))

        val bookSlot = slot<LawBook.Model>()

        coEvery { bookRepository.getSpecific(book1.id) } returnsMany listOf(book1, book2)
        coEvery { userRepository.getSpecific(user.id) } returns user
        coEvery { bookRepository.update(capture(bookSlot)) } returns book1

        val returned = memberService.addMemberToBook(book1.id, user.id)

        assertEquals(6, returned.size)
    }

    @Test
    fun `addMemberToBook throws if it is tried to add the creator to the members`() = runTest {
        val creator = userEntity(4L)
        val toAddUser = userEntity(4L)
        val book = lawBookEntity(1L).copy(creator = creator)

        coEvery { bookRepository.getSpecific(book.id) } returns book
        coEvery { userRepository.getSpecific(toAddUser.id) } returns toAddUser

        assertThrows<ServiceException.CreatorTriedAddedToBook> {
            memberService.addMemberToBook(book.id, toAddUser.id)
        }
    }

    @Test
    fun `removeMemberFromBook throws if either book or user was not found`() = runTest {
        coEvery { bookRepository.getSpecific(1L) } returns null

        assertThrows<ServiceException.LawBookNotFound> {
            memberService.addMemberToBook(1L, 1L)
        }

        coEvery { bookRepository.getSpecific(1L) } returns lawBookEntity(1L)
        coEvery { userRepository.getSpecific(1L) } returns null

        assertThrows<ServiceException.UserNotFound> {
            memberService.removeMemberFromBook(1L, 1L)
        }
    }

    @Test
    fun `removeMemberFromBook throws update if user is not member of the book`() = runTest {
        val members = userEntities(3L)
        val user1 = userEntity(3L)
        val user2 = userEntity(4L)
        val book = lawBookEntity(1L).copy(members = members)

        coEvery { bookRepository.getSpecific(book.id) } returns book
        coEvery { userRepository.getSpecific(user1.id) } returns user1
        coEvery { bookRepository.update(any()) } returns book
        coEvery { userRepository.getSpecific(user2.id) } returns user2

        assertThrows<ServiceException.UserNotMemberOfBook> {
            memberService.removeMemberFromBook(book.id, user2.id)
        }

        memberService.removeMemberFromBook(book.id, user1.id)
        coVerify(exactly = 1) { bookRepository.update(any()) }
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

        memberService.removeMemberFromBook(book.id, user.id)

        assertArrayEquals(
            arrayOf(1L, 3L),
            bookSlot.captured.members.map(me.bumiller.mol.database.table.User.Model::id).sorted().toTypedArray()
        )
    }

    @Test
    fun `removeMemberFromBook returns the updated members`() = runTest {
        val members = userEntities(3L)
        val user = userEntity(4L)
        val book1 = lawBookEntity(1L).copy(members = members)
        val book2 = lawBookEntity(2L).copy(members = userEntities(6L))

        val bookSlot = slot<LawBook.Model>()

        coEvery { bookRepository.getSpecific(book1.id) } returnsMany listOf(book1, book2)
        coEvery { userRepository.getSpecific(user.id) } returns user
        coEvery { bookRepository.update(capture(bookSlot)) } returns book1

        val returned = memberService.addMemberToBook(book1.id, user.id)

        assertEquals(6, returned.size)
    }

    @Test
    fun `setMemberRole throws if user or book not found or user not member of book`() = runTest {
        coEvery { userRepository.getSpecific(1L) } returns null

        assertThrows<ServiceException.UserNotFound> {
            memberService.setMemberRole(1L, 1L, MemberRole.Read)
        }

        coEvery { userRepository.getSpecific(1L) } returns userEntity(1L)
        coEvery { bookRepository.getSpecific(1L) } returns null

        assertThrows<ServiceException.LawBookNotFound> {
            memberService.setMemberRole(1L, 1L, MemberRole.Read)
        }

        coEvery { bookRepository.getSpecific(1L) } returns lawBookEntity(1L).copy(
            members = userEntities(4L).filterNot { it.id == 1L }
        )

        assertThrows<ServiceException.UserNotMemberOfBook> {
            memberService.setMemberRole(1L, 1L, MemberRole.Read)
        }

        coVerify(exactly = 0) { roleRepository.setMemberRole(any(), any(), any()) }

        coVerify(exactly = 3) { userRepository.getSpecific(1L) }
        coVerify(exactly = 2) { bookRepository.getSpecific(1L) }
    }

    @Test
    fun `setMemberRole throws if change would mean no admin in book`() = runTest {
        coEvery { userRepository.getSpecific(1L) } returns userEntity(1L)
        coEvery { bookRepository.getSpecific(1L) } returns lawBookEntity(1L).copy(members = userEntities(4L))

        coEvery { roleRepository.getMemberRole(any(), 1L) } answers { m ->
            when (m.invocation.args[0] as Long) {
                1L -> "admin"
                2L -> "read"
                3L -> "write"
                4L -> "update"
                else -> error(Unit)
            }
        }

        assertThrows<ServiceException.BookNoAdminLeft> {
            memberService.setMemberRole(1L, 1L, MemberRole.Read)
        }

        coVerify(exactly = 4) { roleRepository.getMemberRole(any(), 1L) }
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

        memberService.setMemberRole(1L, 1L, MemberRole.Read)

        coVerify(exactly = 1) { roleRepository.setMemberRole(1L, 1L, "read") }
    }

}