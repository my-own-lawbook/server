package me.bumiller.mol.core

import me.bumiller.mol.core.data.LawContentService

/**
 * Interface to perform complex operations on law-books, law-entries and law-sections.
 *
 * Biggest difference to [LawContentService] is that this allows for operations between the different models.
 */
interface LawService {

    //
    // Books
    //

    /**
     * Checks whether a user is a member of a specific law-book
     *
     * @param userId The id of the user
     * @param bookId The id of the book
     * @return Whether the user is a member of the book
     * @throws IllegalArgumentException If either the book or user cannot be found
     */
    suspend fun isUserMemberOfBook(userId: Long, bookId: Long): Boolean

    //
    // Entries
    //

    /**
     * Checks whether a user is a member of the parent law-book of a specified law-entry
     *
     * @param userId The id of the user
     * @param entryId The id of the law-entry
     * @return Whether the user is a member of the book that contains the entry
     * @throws IllegalArgumentException If either the entry or the user cannot be found
     */
    suspend fun isUserMemberOfEntry(userId: Long, entryId: Long): Boolean

    //
    // Sections
    //

    /**
     * Checks whether the user is a member of the book that contains a specified section
     *
     * @param userId The id of the user
     * @param sectionId The id of the section
     * @return Whether the user is member of the book that contains the section
     * @throws IllegalArgumentException If either the user or the section were not found
     */
    suspend fun isUserMemberOfSection(userId: Long, sectionId: Long): Boolean

}