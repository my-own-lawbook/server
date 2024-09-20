package me.bumiller.mol.core.impl

import me.bumiller.mol.common.Optional
import me.bumiller.mol.common.allNonNullOrNull
import me.bumiller.mol.common.present
import me.bumiller.mol.common.presentWhenNotNull
import me.bumiller.mol.core.data.LawContentService
import me.bumiller.mol.core.exception.ServiceException
import me.bumiller.mol.core.mapping.mapBook
import me.bumiller.mol.core.mapping.mapEntry
import me.bumiller.mol.core.mapping.mapSection
import me.bumiller.mol.database.repository.*
import me.bumiller.mol.database.table.crossref.LawBookMembersCrossref
import me.bumiller.mol.model.LawBook
import me.bumiller.mol.model.LawEntry
import me.bumiller.mol.model.LawSection
import me.bumiller.mol.database.table.LawBook.Model as LawBookModel
import me.bumiller.mol.database.table.LawEntry.Model as LawEntryModel
import me.bumiller.mol.database.table.LawSection.Model as LawSectionModel

internal class DatabaseLawContentService(
    val bookRepository: LawBookRepository,
    val entryRepository: LawEntryRepository,
    val sectionRepository: LawSectionRepository,
    val userRepository: UserRepository,
    val memberRoleRepository: MemberRoleRepository
) : LawContentService {

    override suspend fun getBooks(): List<LawBook> = bookRepository
        .getAll().map(::mapBook)

    override suspend fun getSpecificBook(id: Long?, key: String?): LawBook {
        return bookRepository.getSpecific(
            id = presentWhenNotNull(id),
            key = presentWhenNotNull(key)
        )?.let(::mapBook) ?: throw ServiceException.LawBookNotFound(id = id, key = key)
    }

    override suspend fun getBookByEntry(entryId: Long): LawBook =
        bookRepository.getForEntry(entryId)?.let(::mapBook) ?: throw ServiceException.LawEntryNotFound(id = entryId)

    override suspend fun getBooksForMember(userId: Long): List<LawBook> =
        bookRepository.getAllForMember(userId)?.map(::mapBook) ?: throw ServiceException.UserNotFound(id = userId)

    override suspend fun createBook(key: String, name: String, description: String, creatorId: Long): LawBook {
        val user = userRepository.getSpecific(creatorId) ?: throw ServiceException.UserNotFound(id = creatorId)
        bookRepository.getSpecific(key = present(key))?.let {
            throw ServiceException.LawBookKeyNotUnique(key)
        }

        val book = LawBookModel(
            id = -1,
            key = key,
            name = name,
            description = description,
            creator = user,
            members = listOf(user)
        )

        val created = bookRepository.create(book, user.id)!!
            .let(::mapBook)

        memberRoleRepository.setMemberRole(user.id, created.id, LawBookMembersCrossref.Roles.Admin.serializedName)

        return created
    }

    @Suppress("NAME_SHADOWING")
    override suspend fun updateBook(
        bookId: Long,
        key: Optional<String>,
        name: Optional<String>,
        description: Optional<String>,
        creatorId: Optional<Long>,
        memberIds: Optional<List<Long>>
    ): LawBook {
        val book = bookRepository.getSpecific(bookId) ?: throw ServiceException.LawBookNotFound(id = bookId)
        key.ifPresentSuspend {
            bookRepository.getSpecific(key = present(it))?.let {
                throw ServiceException.LawBookKeyNotUnique(key.get())
            }
        }

        val creator = creatorId.mapSuspend {
            userRepository.getSpecific(it)
        }
        val members = memberIds.mapSuspend { memberIds ->
            memberIds.map { memberId -> userRepository.getSpecific(memberId) }
        }

        val updatedBook = book.copy(
            key = key.getOr(book.key),
            name = name.getOr(book.name),
            description = description.getOr(book.description),
            creator = creator.getOr(book.creator) ?: throw ServiceException.UserNotFound(id = creatorId.get()),
            members = members.getOr(book.members).allNonNullOrNull() ?: throw ServiceException.UserNotFoundList(
                memberIds.get()
            )
        )

        return bookRepository.update(updatedBook)!!.let(::mapBook)
    }

    override suspend fun deleteBook(id: Long): LawBook = bookRepository
        .delete(id)?.let(::mapBook) ?: throw ServiceException.LawBookNotFound(id = id)

    override suspend fun getEntries(): List<LawEntry> = entryRepository
        .getAll().map(::mapEntry)

    override suspend fun getEntriesByBook(bookId: Long): List<LawEntry> {
        bookRepository.getSpecific(bookId) ?: throw ServiceException.LawBookNotFound(id = bookId)

        return entryRepository.getForParentBook(bookId)
            .map(::mapEntry)
    }

    override suspend fun getSpecificEntry(
        id: Optional<Long>,
        key: Optional<String>,
        parentBookId: Optional<Long>
    ): LawEntry {
        parentBookId.ifPresentSuspend {
            bookRepository.getSpecific(id = parentBookId.get())?.let {
                throw ServiceException.LawBookNotFound(id = parentBookId.get())
            }
        }

        return entryRepository.getSpecific(
            id = id,
            key = key,
            parentBookId = parentBookId,
        )?.let(::mapEntry) ?: throw ServiceException.LawEntryNotFound(id = id.getOrNull(), key = key.getOrNull())
    }

    override suspend fun getEntryForSection(sectionId: Long): LawEntry =
        entryRepository.getForSection(sectionId)
            ?.let(::mapEntry) ?: throw ServiceException.LawSectionNotFound(id = sectionId)

    override suspend fun createEntry(key: String, name: String, parentBookId: Long): LawEntry {
        bookRepository.getSpecific(parentBookId) ?: throw ServiceException.LawBookNotFound(id = parentBookId)
        entryRepository.getSpecific(key = present(key), parentBookId = present(parentBookId))?.let {
            throw ServiceException.LawEntryKeyNotUnique(key)
        }

        val entry = LawEntryModel(
            id = -1,
            key = key,
            name = name
        )

        return entryRepository.create(entry, parentBookId)
            ?.let(::mapEntry) ?: throw ServiceException.LawBookNotFound(parentBookId)
    }

    override suspend fun updateEntry(
        entryId: Long,
        key: Optional<String>,
        name: Optional<String>
    ): LawEntry {
        val entry = entryRepository.getSpecific(entryId) ?: throw ServiceException.LawEntryNotFound(id = entryId)
        val parentBook = bookRepository.getForEntry(entryId)!!
        key.ifPresentSuspend {
            entryRepository.getSpecific(key = key, parentBookId = present(parentBook.id))?.let {
                throw ServiceException.LawEntryKeyNotUnique(key.get())
            }
        }

        val updatedEntry = entry.copy(
            key = key.getOr(entry.key),
            name = name.getOr(entry.name)
        )

        return entryRepository.update(updatedEntry)!!
            .let(::mapEntry)
    }

    override suspend fun deleteEntry(id: Long): LawEntry = entryRepository
        .delete(id)
        ?.let(::mapEntry) ?: throw ServiceException.LawEntryNotFound(id = id)

    override suspend fun getSections(): List<LawSection> = sectionRepository
        .getAll().map(::mapSection)

    override suspend fun updateSection(
        sectionId: Long,
        index: Optional<String>,
        name: Optional<String>,
        content: Optional<String>
    ): LawSection {
        val section =
            sectionRepository.getSpecific(sectionId) ?: throw ServiceException.LawSectionNotFound(id = sectionId)

        val parentEntry = entryRepository.getForSection(sectionId)!!
        index.ifPresentSuspend {
            sectionRepository.getSpecific(index = index, parentEntryId = present(parentEntry.id))?.let {
                throw ServiceException.LawSectionIndexNotUnique(index.get())
            }
        }

        val updatedSection = section.copy(
            index = index.getOr(section.index),
            name = name.getOr(section.name),
            content = content.getOr(section.content)
        )

        return sectionRepository.update(updatedSection)!!
            .let(::mapSection)
    }

    override suspend fun deleteSection(id: Long): LawSection = sectionRepository
        .delete(id)
        ?.let(::mapSection) ?: throw ServiceException.LawSectionNotFound(id = id)

    override suspend fun getSectionsByEntry(entryId: Long): List<LawSection> {
        entryRepository.getSpecific(entryId) ?: throw ServiceException.LawEntryNotFound(id = entryId)

        return sectionRepository.getForParentEntry(entryId)
            .map(::mapSection)
    }

    override suspend fun getSpecificSection(
        id: Optional<Long>,
        index: Optional<String>,
        parentEntryId: Optional<Long>
    ): LawSection {
        parentEntryId.ifPresentSuspend {
            entryRepository.getSpecific(it) ?: throw ServiceException.LawEntryNotFound(id = it)
        }

        return sectionRepository
            .getSpecific(id, index, parentEntryId)?.let(::mapSection)
            ?: throw ServiceException.LawSectionNotFound(id = id.getOrNull(), index = index.getOrNull())
    }

    override suspend fun createSection(index: String, name: String, content: String, parentEntryId: Long): LawSection {
        entryRepository.getSpecific(parentEntryId) ?: throw ServiceException.LawEntryNotFound(id = parentEntryId)
        sectionRepository.getSpecific(index = present(index), parentEntryId = present(parentEntryId))?.let {
            throw ServiceException.LawSectionIndexNotUnique(index)
        }

        val section = LawSectionModel(
            id = -1,
            index = index,
            name = name,
            content = content
        )

        return sectionRepository.create(section, parentEntryId)
            ?.let(::mapSection) ?: throw ServiceException.LawEntryNotFound(parentEntryId)
    }
}