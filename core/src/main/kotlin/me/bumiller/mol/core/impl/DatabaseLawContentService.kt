package me.bumiller.mol.core.impl

import me.bumiller.mol.common.Optional
import me.bumiller.mol.common.allNonNullOrNull
import me.bumiller.mol.common.presentWhenNotNull
import me.bumiller.mol.core.data.LawContentService
import me.bumiller.mol.core.mapping.mapBook
import me.bumiller.mol.core.mapping.mapEntry
import me.bumiller.mol.core.mapping.mapSection
import me.bumiller.mol.database.repository.LawBookRepository
import me.bumiller.mol.database.repository.LawEntryRepository
import me.bumiller.mol.database.repository.LawSectionRepository
import me.bumiller.mol.database.repository.UserRepository
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
    val userRepository: UserRepository
) : LawContentService {

    override suspend fun getBooks(): List<LawBook> = bookRepository
        .getAll().map(::mapBook)

    override suspend fun getBooksByCreator(userId: Long): List<LawBook>? {
        userRepository.getSpecific(userId) ?: return null

        return bookRepository.getForCreator(userId)
            .map(::mapBook)
    }

    override suspend fun getSpecificBook(id: Long?, key: String?, creatorId: Long?): LawBook? =
        bookRepository.getSpecific(
            id = presentWhenNotNull(id),
            creatorId = presentWhenNotNull(creatorId),
            key = presentWhenNotNull(key)
        )?.let(::mapBook)

    override suspend fun createBook(key: String, name: String, description: String, creatorId: Long): LawBook? {
        val user = userRepository.getSpecific(creatorId) ?: return null
        val book = LawBookModel(
            id = -1,
            key = key,
            name = name,
            description = description,
            creator = user,
            members = listOf()
        )

        return bookRepository.create(book, user.id)!!
            .let(::mapBook)
    }

    @Suppress("NAME_SHADOWING")
    override suspend fun updateBook(
        bookId: Long,
        key: Optional<String>,
        name: Optional<String>,
        description: Optional<String>,
        creatorId: Optional<Long>,
        memberIds: Optional<List<Long>>
    ): LawBook? {
        val book = bookRepository.getSpecific(bookId) ?: return null
        val creator = creatorId.mapSuspend {
            userRepository.getSpecific(creatorId)
        }
        val members = memberIds.mapSuspend { memberIds ->
            memberIds.map { memberId -> userRepository.getSpecific(memberId) }
        }

        val updatedBook = book.copy(
            key = key.getOr(book.key),
            name = name.getOr(book.name),
            description = description.getOr(book.description),
            creator = creator.getOr(book.creator) ?: return null,
            members = members.getOr(book.members).allNonNullOrNull() ?: return null
        )

        return bookRepository.update(updatedBook)
            ?.let(::mapBook)
    }

    override suspend fun getEntries(): List<LawEntry> = entryRepository
        .getAll().map(::mapEntry)

    override suspend fun getEntriesByBook(bookId: Long): List<LawEntry>? {
        bookRepository.getSpecific(bookId) ?: return null

        return entryRepository.getForParentBook(bookId)
            .map(::mapEntry)
    }

    override suspend fun getSpecificEntry(
        id: Optional<Long>,
        key: Optional<String>,
        parentBookId: Optional<Long>
    ): LawEntry? = entryRepository
        .getSpecific(
            id = id,
            key = key,
            parentBookId = parentBookId,
        )?.let(::mapEntry)

    override suspend fun createEntry(key: String, name: String, parentBookId: Long): LawEntry? {
        bookRepository.getSpecific(parentBookId) ?: return null
        val entry = LawEntryModel(
            id = -1,
            key = key,
            name = name
        )

        return entryRepository.create(entry)
            .let(::mapEntry)
    }

    override suspend fun updateEntry(
        entryId: Long,
        key: Optional<String>,
        name: Optional<String>,
        parentBookId: Optional<Long>
    ): LawEntry? {
        if (parentBookId is Optional.Present) {
            bookRepository.getSpecific(parentBookId.get()) ?: return null
        }
        val entry = entryRepository.getSpecific(entryId) ?: return null

        val updatedEntry = entry.copy(
            key = key.getOr(entry.key),
            name = name.getOr(entry.name)
        )
        if (parentBookId is Optional.Present) {
            entryRepository.updateParentBook(entryId, parentBookId.get()) ?: return null
        }

        return entryRepository.update(updatedEntry)
            ?.let(::mapEntry)
    }

    override suspend fun getSections(): List<LawSection> = sectionRepository
        .getAll().map(::mapSection)

    override suspend fun updateSection(
        sectionId: Long,
        index: Optional<String>,
        name: Optional<String>,
        content: Optional<String>,
        parentEntryId: Optional<Long>
    ): LawSection? {
        if (parentEntryId is Optional.Present) {
            entryRepository.getSpecific(parentEntryId) ?: return null
        }
        val section = sectionRepository.getSpecific(sectionId) ?: return null

        val updatedSection = section.copy(
            index = index.getOr(section.index),
            name = index.getOr(section.name),
            content = index.getOr(section.content)
        )

        if (parentEntryId is Optional.Present) {
            sectionRepository.updateParentEntry(sectionId, parentEntryId.get()) ?: return null
        }

        return sectionRepository.update(updatedSection)?.let(::mapSection)
    }

    override suspend fun getSectionsByEntry(entryId: Long): List<LawSection>? {
        entryRepository.getSpecific(entryId) ?: return null

        return sectionRepository.getForParentEntry(entryId)
            .map(::mapSection)
    }

    override suspend fun getSpecificSection(
        id: Optional<Long>,
        index: Optional<String>,
        parentEntryId: Optional<Long>
    ): LawSection? = sectionRepository
        .getSpecific(id, index, parentEntryId)?.let(::mapSection)

    override suspend fun createSection(index: String, name: String, content: String, parentEntryId: Long): LawSection? {
        entryRepository.getSpecific(parentEntryId) ?: return null

        val section = LawSectionModel(
            id = -1,
            index = index,
            name = name,
            content = content
        )

        return sectionRepository.create(section)
            .let(::mapSection)
    }
}