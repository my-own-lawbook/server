package me.bumiller.mol.core.impl

import io.mockk.*
import kotlinx.coroutines.test.runTest
import me.bumiller.mol.common.Optional
import me.bumiller.mol.common.empty
import me.bumiller.mol.common.present
import me.bumiller.mol.core.data.LawContentService
import me.bumiller.mol.core.exception.ServiceException
import me.bumiller.mol.database.repository.*
import me.bumiller.mol.database.table.LawSection
import me.bumiller.mol.model.LawBook
import me.bumiller.mol.test.util.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DatabaseLawContentServiceTest {

    private lateinit var bookRepository: LawBookRepository
    private lateinit var entryRepository: LawEntryRepository
    private lateinit var sectionRepository: LawSectionRepository
    private lateinit var userRepository: UserRepository
    private lateinit var memberRoleRepository: MemberRoleRepository

    private lateinit var lawContentService: LawContentService

    @BeforeEach
    fun setup() {
        bookRepository = mockk()
        entryRepository = mockk()
        sectionRepository = mockk()
        userRepository = mockk()
        memberRoleRepository = mockk()

        lawContentService =
            DatabaseLawContentService(
                bookRepository,
                entryRepository,
                sectionRepository,
                userRepository,
                memberRoleRepository
            )

        coEvery { bookRepository.getAll() } returns lawBookEntities(5)
    }

    @Test
    fun `getBooks returns books by from the repository`() = runTest {
        val returned = lawContentService.getBooks()

        assertArrayEquals((1..5L).toList().toTypedArray(), returned.map(LawBook::id).toTypedArray())
    }

    @Test
    fun `getSpecificBook properly maps nullables to optionals`() = runTest {
        val idSlots = mutableListOf<Optional<Long>>()
        val keySlots = mutableListOf<Optional<String>>()

        coEvery { userRepository.getSpecific(any<Long>()) } returns userEntity(1L)
        coEvery {
            bookRepository.getSpecific(
                capture(idSlots),
                capture(keySlots)
            )
        } returns lawBookEntity(1L)

        lawContentService.getSpecificBook(null, null)
        assertFalse(idSlots[0].isPresent)
        assertFalse(keySlots[0].isPresent)

        lawContentService.getSpecificBook(1L, null)
        assertEquals(1L, idSlots[1].get())
        assertFalse(keySlots[1].isPresent)

        lawContentService.getSpecificBook(1L, "fds")
        assertEquals(1L, idSlots[2].get())
        assertEquals("fds", keySlots[2].get())

        lawContentService.getSpecificBook(null, null)
        assertFalse(idSlots[3].isPresent)
        assertFalse(keySlots[3].isPresent)
    }

    @Test
    fun `getSpecificBook throws when it is not found`() = runTest {
        coEvery { bookRepository.getSpecific(any(), any()) } returns null

        assertThrows<ServiceException.LawBookNotFound> {
            lawContentService.getSpecificBook()
        }
    }

    @Test
    fun `getBookByEntry calls repository method with right argument`() = runTest {
        val idSlots = mutableListOf<Long>()

        coEvery { bookRepository.getForEntry(capture(idSlots)) } returns lawBookEntity(1L)

        lawContentService.getBookByEntry(4L)
        lawContentService.getBookByEntry(8L)

        assertEquals(4L, idSlots.first())
        assertEquals(8L, idSlots.component2())
    }

    @Test
    fun `getBookByEntry returns book returned by repository`() = runTest {
        coEvery { bookRepository.getForEntry(any()) } returns lawBookEntity(5L)

        val returned1 = lawContentService.getBookByEntry(4L)
        assertEquals(5L, returned1.id)
    }

    @Test
    fun `getBooksForMember calls repository method with right argument`() = runTest {
        val idSlots = mutableListOf<Long>()

        coEvery { bookRepository.getAllForMember(capture(idSlots)) } returns lawBookEntities(4)

        lawContentService.getBooksForMember(4L)
        lawContentService.getBooksForMember(8L)

        assertEquals(4L, idSlots.first())
        assertEquals(8L, idSlots.component2())
    }

    @Test
    fun `getBooksForMember returns books returned by repository`() = runTest {
        coEvery { bookRepository.getAllForMember(any()) } returns lawBookEntities(9)

        val returned1 = lawContentService.getBooksForMember(4L)
        assertEquals(9, returned1.size)

        coEvery { bookRepository.getAllForMember(any()) } returns emptyList()

        val returned2 = lawContentService.getBooksForMember(4L)
        assertEquals(0, returned2.size)
    }

    @Test
    fun `createBook throws when user is not found`() = runTest {
        coEvery { userRepository.getSpecific(any<Long>()) } returns null

        assertThrows<ServiceException.UserNotFound> {
            lawContentService.createBook("", "", "", 1L)
        }
    }

    @Test
    fun `createBook throws when key is not unique`() = runTest {
        coEvery { userRepository.getSpecific(any<Long>()) } returns userEntity(1L)
        coEvery { bookRepository.getSpecific(any(), any()) } returns lawBookEntity(1L)

        assertThrows<ServiceException.LawBookKeyNotUnique> {
            lawContentService.createBook("", "", "", 1L)
        }
    }

    @Test
    fun `createBook calls repository with correctly created book model and user id and returns created book`() =
        runTest {
            coEvery { userRepository.getSpecific(any<Long>()) } returns userEntity(12L)
            coEvery { bookRepository.getSpecific(any(), any()) } returns null
            coEvery { memberRoleRepository.setMemberRole(any(), any(), any()) } just runs

            val bookSlot = slot<me.bumiller.mol.database.table.LawBook.Model>()
            val userIdSlot = slot<Long>()
            coEvery { bookRepository.create(capture(bookSlot), capture(userIdSlot)) } returnsArgument 0

            val returned = lawContentService.createBook("key153", "name", "description", 12L)

            bookSlot.captured.run {
                assertEquals(-1, id)
                assertEquals("key153", key)
                assertEquals("name", name)
                assertEquals("description", description)
                assertEquals(12L, creator.id)
                assertEquals(1, members.size)
                assertEquals(12L, members.first().id)
            }
            assertEquals(12L, userIdSlot.captured)
            coVerify(exactly = 1) { bookRepository.create(any(), any()) }

            assertEquals(-1L, returned.id)
            assertEquals("key153", returned.key)
        }

    @Test
    fun `createBook sets role of creator to admin after creating the book`() =
        runTest {
            coEvery { userRepository.getSpecific(any<Long>()) } returns userEntity(12L)
            coEvery { bookRepository.getSpecific(any(), any()) } returns null
            coEvery { memberRoleRepository.setMemberRole(any(), any(), any()) } just runs

            val bookSlot = slot<me.bumiller.mol.database.table.LawBook.Model>()
            val userIdSlot = slot<Long>()
            coEvery { bookRepository.create(capture(bookSlot), capture(userIdSlot)) } returnsArgument 0

            val returned = lawContentService.createBook("key153", "name", "description", 12L)

            coVerify { memberRoleRepository.setMemberRole(12L, any(), "admin") }
        }

    @Test
    fun `updateBook throws when book is not found`() = runTest {
        coEvery { bookRepository.getSpecific(any<Long>()) } returns null

        assertThrows<ServiceException.LawBookNotFound> {
            lawContentService.updateBook(1L)
        }
    }

    @Test
    fun `updateBook fetches creator and member ids from database only when id's are passed`() = runTest {
        coEvery { bookRepository.getSpecific(any<Long>()) } returns lawBookEntity(1L)
        coEvery { userRepository.getSpecific(any<Long>()) } returns userEntity(1L)
        coEvery { bookRepository.update(any()) } returnsArgument 0

        lawContentService.updateBook(
            bookId = 1L,
            creatorId = present(5L),
            memberIds = present(listOf(4L, 3L, 8L, 5))
        )
        lawContentService.updateBook(
            bookId = 1L,
            creatorId = empty(),
            memberIds = present(listOf(4L, 3L, 8L, 5))
        )

        coVerify(exactly = 9) { userRepository.getSpecific(any<Long>()) }
    }

    @Test
    fun `updateBook returns updated book`() = runTest {
        coEvery { bookRepository.getSpecific(any<Long>()) } returns lawBookEntity(1L)
        coEvery { userRepository.getSpecific(any<Long>()) } returns userEntity(1L)
        coEvery { bookRepository.update(any()) } returnsArgument 0

        val returned = lawContentService.updateBook(1L)

        assertEquals(lawBookModel(1L), returned)
    }

    @Test
    fun `deleteBook returns deleted book or throws not found`() = runTest {
        coEvery { bookRepository.delete(any<Long>()) } returns null

        assertThrows<ServiceException.LawBookNotFound> {
            lawContentService.deleteBook(1L)
        }

        coEvery { bookRepository.delete(any<Long>()) } returns lawBookEntity(1L)

        val returned2 = lawContentService.deleteBook(1L)

        assertEquals(lawBookModel(1L), returned2)
    }

    @Test
    fun `getEntries returns all entries from repository`() = runTest {
        coEvery { entryRepository.getAll() } returns lawEntryEntities(5L)

        val returned = lawContentService.getEntries()

        assertEquals(5, returned.size)
    }

    @Test
    fun `getEntriesByBook throws if book not found`() = runTest {
        coEvery { bookRepository.getSpecific(any<Long>()) } returns null

        assertThrows<ServiceException.LawBookNotFound> {
            lawContentService.getEntriesByBook(1L)
        }
    }

    @Test
    fun `getEntriesByBook calls repository method with correct argument and returns result`() = runTest {
        coEvery { bookRepository.getSpecific(any<Long>()) } returns lawBookEntity(1L)
        val idSlot = slot<Long>()
        coEvery { entryRepository.getForParentBook(capture(idSlot)) } returns lawEntryEntities(5)

        val returned = lawContentService.getEntriesByBook(1L)

        assertEquals(1L, idSlot.captured)
        assertEquals(5, returned.size)
    }

    @Test
    fun `getSpecificEntry correctly passes arguments and returns result`() = runTest {
        val idSlots = mutableListOf<Optional<Long>>()
        val keySlots = mutableListOf<Optional<String>>()
        val parentIdSlots = mutableListOf<Optional<Long>>()

        coEvery { bookRepository.getSpecific(any<Long>()) } returns null
        coEvery {
            entryRepository.getSpecific(
                capture(idSlots),
                capture(keySlots),
                capture(parentIdSlots)
            )
        } returns lawEntryEntity(99L)

        lawContentService.getSpecificEntry(
            id = empty(),
            key = empty(),
            parentBookId = empty()
        )
        assertFalse(idSlots[0].isPresent)
        assertFalse(keySlots[0].isPresent)
        assertFalse(parentIdSlots[0].isPresent)

        val returned = lawContentService.getSpecificEntry(
            id = present(3L),
            key = empty(),
            parentBookId = present(5L)
        )
        assertEquals(3L, idSlots[1].get())
        assertFalse(keySlots[1].isPresent)
        assertEquals(5L, parentIdSlots[1].get())

        assertEquals(lawEntryModel(99L), returned)
    }

    @Test
    fun `getEntryForSection returns from repository or throws if null`() = runTest {
        coEvery { entryRepository.getForSection(any<Long>()) } returns null

        assertThrows<ServiceException.LawSectionNotFound> {
            lawContentService.getEntryForSection(1L)
        }

        coEvery { entryRepository.getForSection(any<Long>()) } returns lawEntryEntity(34L)

        val returned2 = lawContentService.getEntryForSection(4L)
        assertEquals(lawEntryModel(34L), returned2)
    }

    @Test
    fun `createEntry correctly creates entry model and returns result`() = runTest {
        coEvery { bookRepository.getSpecific(any<Long>()) } returns lawBookEntity(22L)
        coEvery { entryRepository.getSpecific(any(), any(), any()) } returns null

        val entrySlot = slot<me.bumiller.mol.database.table.LawEntry.Model>()
        coEvery { entryRepository.create(capture(entrySlot), any()) } returnsArgument 0

        val returned = lawContentService.createEntry(
            key = "key82",
            name = "name",
            parentBookId = 3L
        )

        assertEquals(-1L, entrySlot.captured.id)
        assertEquals("key82", entrySlot.captured.key)

        assertEquals(-1L, returned.id)
        assertEquals("key82", returned.key)
    }

    @Test
    fun `updateEntry throws if entry is not found`() = runTest {
        coEvery { entryRepository.getSpecific(any<Long>()) } returns null

        assertThrows<ServiceException.LawEntryNotFound> {
            lawContentService.updateEntry(1L)
        }
    }

    @Test
    fun `updateEntry correctly updates only passed arguments and returns result`() = runTest {
        coEvery { entryRepository.getSpecific(any<Long>()) } returns lawEntryEntity(4L)
        coEvery { entryRepository.getSpecific(any(), any(), any()) } returns null
        coEvery { bookRepository.getForEntry(any()) } returns lawBookEntity(1L)
        val entrySlot = slot<me.bumiller.mol.database.table.LawEntry.Model>()

        coEvery { entryRepository.update(capture(entrySlot)) } returnsArgument 0

        val returned = lawContentService.updateEntry(
            entryId = 1L,
            key = present("key-8292")
        )

        assertEquals("key-8292", returned.key)
        assertEquals("name-4", returned.name)

        assertEquals("key-8292", entrySlot.captured.key)
        assertEquals("name-4", entrySlot.captured.name)
    }

    @Test
    fun `deleteEntry returns deleted entry or throws if not found`() = runTest {
        coEvery { entryRepository.delete(any<Long>()) } returns null

        assertThrows<ServiceException.LawEntryNotFound> {
            lawContentService.deleteEntry(1L)
        }

        coEvery { entryRepository.delete(any<Long>()) } returns lawEntryEntity(32L)

        val returned2 = lawContentService.deleteEntry(1L)
        assertEquals(lawEntryModel(32L), returned2)
    }

    @Test
    fun `getSections returns values from repository`() = runTest {
        coEvery { sectionRepository.getAll() } returns lawSectionEntities(7L)

        val returned = lawContentService.getSections()
        assertEquals(7, returned.size)
    }

    @Test
    fun `updateSection correctly updates only passed arguments and returns result`() = runTest {
        coEvery { sectionRepository.getSpecific(any<Long>()) } returns lawSectionEntity(78L)
        coEvery { entryRepository.getForSection(any()) } returns lawEntryEntity(1L)
        coEvery { sectionRepository.getSpecific(any(), any(), any()) } returns null

        val entitySlot = slot<LawSection.Model>()
        coEvery { sectionRepository.update(capture(entitySlot)) } returnsArgument 0

        val returned = lawContentService.updateSection(
            sectionId = 4L,
            index = present("index-593"),
            content = present("content-234")
        )

        assertEquals("index-593", entitySlot.captured.index)
        assertEquals("name-78", entitySlot.captured.name)
        assertEquals("content-234", entitySlot.captured.content)

        assertEquals("index-593", returned.index)
        assertEquals("name-78", returned.name)
        assertEquals("content-234", returned.content)
    }

    @Test
    fun `deleteSection returns deleted section or throws if not found`() = runTest {
        coEvery { sectionRepository.delete(any<Long>()) } returns null

        assertThrows<ServiceException.LawSectionNotFound> {
            lawContentService.deleteSection(1L)
        }

        coEvery { sectionRepository.delete(any<Long>()) } returns lawSectionEntity(34L)

        val returned2 = lawContentService.deleteSection(1L)
        assertEquals(34L, returned2.id)
    }


    @Test
    fun `getSectionsByEntry throws if entry was not found`() = runTest {
        coEvery { entryRepository.getSpecific(any<Long>()) } returns null

        assertThrows<ServiceException.LawEntryNotFound> {
            lawContentService.getSectionsByEntry(1L)
        }
    }

    @Test
    fun `getSectionsByEntry returns values from repository`() = runTest {
        coEvery { entryRepository.getSpecific(any<Long>()) } returns lawEntryEntity(1L)
        coEvery { sectionRepository.getForParentEntry(any<Long>()) } returns lawSectionEntities(6)

        val returned = lawContentService.getSectionsByEntry(1L)
        assertEquals(6, returned.size)
    }

    @Test
    fun `getSpecificSection correctly passes arguments and returns value`() = runTest {
        val idSlot = mutableListOf<Optional<Long>>()
        val indexSlot = mutableListOf<Optional<String>>()
        val parentIdSlot = mutableListOf<Optional<Long>>()

        coEvery {
            sectionRepository.getSpecific(
                capture(idSlot),
                capture(indexSlot),
                capture(parentIdSlot)
            )
        } returns lawSectionEntity(12L)

        lawContentService.getSpecificSection()
        assertFalse(idSlot[0].isPresent)
        assertFalse(indexSlot[0].isPresent)
        assertFalse(parentIdSlot[0].isPresent)

        val returned = lawContentService.getSpecificSection(
            id = present(8L)
        )
        assertEquals(8L, idSlot[1].get())
        assertFalse(indexSlot[1].isPresent)
        assertFalse(parentIdSlot[1].isPresent)

        assertEquals(12L, returned.id)
    }

    @Test
    fun `createSection throws if parent entry was not found`() = runTest {
        coEvery { entryRepository.getSpecific(any<Long>()) } returns null

        assertThrows<ServiceException.LawEntryNotFound> {
            lawContentService.createSection("", "", "", 1L)
        }
    }

    @Test
    fun `createSection creates correct entity`() = runTest {
        coEvery { entryRepository.getSpecific(any<Long>()) } returns lawEntryEntity(3L)
        coEvery { sectionRepository.getSpecific(any(), any(), any()) } returns null

        val entitySlot = slot<LawSection.Model>()
        coEvery { sectionRepository.create(capture(entitySlot), any()) } returnsArgument 0


        val returned = lawContentService.createSection("index-2", "name-3", "content-4", 1L)

        assertEquals("index-2", entitySlot.captured.index)
        assertEquals("name-3", entitySlot.captured.name)
        assertEquals("content-4", entitySlot.captured.content)

        assertEquals("index-2", returned.index)
        assertEquals("name-3", returned.name)
        assertEquals("content-4", returned.content)
    }

}