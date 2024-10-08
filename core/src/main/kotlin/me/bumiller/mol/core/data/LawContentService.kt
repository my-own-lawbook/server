package me.bumiller.mol.core.data

import me.bumiller.mol.common.Optional
import me.bumiller.mol.common.empty
import me.bumiller.mol.core.exception.ServiceException
import me.bumiller.mol.model.LawBook
import me.bumiller.mol.model.LawEntry
import me.bumiller.mol.model.LawSection

/**
 * Interface to query and perform actions on the law content stored in the database
 */
interface LawContentService {

    //
    // Law-Books
    //

    /**
     * Gets all the [LawBook]s
     *
     * @return All [LawBook]s
     */
    suspend fun getBooks(): List<LawBook>

    /**
     * Gets a specific [LawBook] matching all the given criteria
     *
     * @param id The id of the [LawBook]
     * @param key The key of the [LawBook]
     * @return The singular [LawBook]
     * @throws ServiceException.LawBookNotFound If the book could not be found
     */
    suspend fun getSpecificBook(
        id: Long? = null,
        key: String? = null
    ): LawBook

    /**
     * Gets the parent [LawBook] of a specific entry
     *
     * @param entryId The id of the entry
     * @return The parent [LawBook]
     * @throws ServiceException.LawEntryNotFound If the entry was not found
     */
    suspend fun getBookByEntry(entryId: Long): LawBook

    /**
     * Gets all [LawBook]s for a specific user
     *
     * @param userId The id of the user
     * @return All [LawBook]s the user is a member of
     * @throws ServiceException.UserNotFound If the user was not found
     */
    suspend fun getBooksForMember(userId: Long): List<LawBook>

    /**
     * Creates a new [LawBook] and saves it to the database
     *
     * @param key The key
     * @param name The name
     * @param description The description
     * @param creatorId The id of the user that created it
     * @return The created book, or null if the user was not found or the [key] was already present
     * @throws ServiceException.UserNotFound If the user was not found
     * @throws ServiceException.LawBookKeyNotUnique If the [key] already exists
     */
    suspend fun createBook(
        key: String,
        name: String,
        description: String,
        creatorId: Long
    ): LawBook

    /**
     * Updates an existing [LawBook]
     *
     * @param bookId The id of the book to update
     * @param key The key
     * @param name The name
     * @param description The description
     * @param memberIds The ids of the new members
     * @return The updated [LawBook]
     * @throws ServiceException.UserNotFoundList If any of the [memberIds] did not resolve to a user
     * @throws ServiceException.LawBookNotFound If the law-book was not found
     * @throws ServiceException.LawBookKeyNotUnique If the [key] already existed
     */
    suspend fun updateBook(
        bookId: Long,
        key: Optional<String> = empty(),
        name: Optional<String> = empty(),
        description: Optional<String> = empty(),
        memberIds: Optional<List<Long>> = empty()
    ): LawBook

    /**
     * Deletes a [LawBook]
     *
     * @param id The id of the book
     * @return The deleted book
     * @throws ServiceException.LawBookNotFound If the book could not be found
     */
    suspend fun deleteBook(id: Long): LawBook

    //
    // Law-Entries
    //

    /**
     * Gets all [LawEntry]s
     *
     * @return All [LawEntry]s
     */
    suspend fun getEntries(): List<LawEntry>

    /**
     * Gets all [LawEntry]s of a specific [LawBook]
     *
     * @param bookId The id of the book
     * @return All entries
     * @throws ServiceException.LawBookNotFound If the law-book was not found
     */
    suspend fun getEntriesByBook(bookId: Long): List<LawEntry>

    /**
     * Gets a specific [LawEntry] matching all given criteria
     *
     * @param id The id
     * @param key The key
     * @param parentBookId The id of the book that the entry is part of
     * @return The singular [LawEntry]
     * @throws ServiceException.LawEntryNotFound If no entry was found
     * @throws ServiceException.LawBookNotFound If the parent book was not found
     */
    suspend fun getSpecificEntry(
        id: Optional<Long> = empty(),
        key: Optional<String> = empty(),
        parentBookId: Optional<Long> = empty()
    ): LawEntry

    /**
     * Gets the parent [LawEntry] for a specific section
     *
     * @param sectionId The id of the section
     * @return The parent [LawEntry] of the specific section
     * @throws ServiceException.LawSectionNotFound If the section was not found
     */
    suspend fun getEntryForSection(sectionId: Long): LawEntry

    /**
     * Creates a new [LawEntry]
     *
     * @param key The key
     * @param name The name
     * @param parentBookId The id of the parent book
     * @return The created [LawEntry]
     * @throws ServiceException.LawBookNotFound If the parent book was not found
     * @throws ServiceException.LawEntryKeyNotUnique If the [key] already exists in the book
     */
    suspend fun createEntry(
        key: String,
        name: String,
        parentBookId: Long
    ): LawEntry

    /**
     * Updates a specific [LawEntry]
     *
     * @param entryId The id of the entry
     * @param key The key
     * @param name The name
     * @return The updated [LawEntry]
     * @throws ServiceException.LawBookNotFound If the entry was not found
     * @throws ServiceException.LawEntryKeyNotUnique If the [key] already existed in the book
     */
    suspend fun updateEntry(
        entryId: Long,
        key: Optional<String> = empty(),
        name: Optional<String> = empty()
    ): LawEntry

    /**
     * Deletes a [LawEntry]
     *
     * @param id The id of the entry
     * @return The deleted [LawEntry]
     * @throws ServiceException.LawEntryNotFound If the entry was not found
     */
    suspend fun deleteEntry(id: Long): LawEntry

    //
    // Law-Section
    //

    /**
     * Gets all [LawSection]s
     *
     * @return All [LawSection]s
     */
    suspend fun getSections(): List<LawSection>

    /**
     * Gets all [LawSection]s of a specific [LawEntry]
     *
     * @param entryId The id of the entry
     * @return All sections
     * @throws ServiceException.LawEntryNotFound If the entry was not found
     */
    suspend fun getSectionsByEntry(entryId: Long): List<LawSection>

    /**
     * Gets a specific [LawSection] matching all given criteria
     *
     * @param id The id
     * @param index The index
     * @param parentEntryId The id of the entry that the section is part of
     * @return The singular [LawSection]
     * @throws ServiceException.LawSectionNotFound If the section was not found
     * @throws ServiceException.LawEntryNotFound If the entry was not found
     */
    suspend fun getSpecificSection(
        id: Optional<Long> = empty(),
        index: Optional<String> = empty(),
        parentEntryId: Optional<Long> = empty()
    ): LawSection

    /**
     * Creates a new [LawSection]
     *
     * @param index The index
     * @param name The name
     * @param content The content
     * @param parentEntryId The id of the parent entry
     * @return The created [LawSection]
     * @throws ServiceException.LawEntryNotFound If the entry was not found
     * @throws ServiceException.LawSectionIndexNotUnique If the index already existed in the parent
     */
    suspend fun createSection(
        index: String,
        name: String,
        content: String,
        parentEntryId: Long
    ): LawSection

    /**
     * Updates a specific [LawSection]
     *
     * @param sectionId The id of the entry
     * @param index The index
     * @param name The name
     * @param content The content
     * @return The updated [LawSection]
     * @throws ServiceException.LawSectionNotFound If the section was not found
     * @throws ServiceException.LawSectionIndexNotUnique If the index is already present in the parent
     */
    suspend fun updateSection(
        sectionId: Long,
        index: Optional<String> = empty(),
        name: Optional<String> = empty(),
        content: Optional<String> = empty()
    ): LawSection

    /**
     * Deletes a [LawSection]
     *
     * @param id The id of the [LawSection]
     * @return The deleted [LawSection]
     * @throws ServiceException.LawSectionNotFound If the section was not found
     */
    suspend fun deleteSection(id: Long): LawSection

}