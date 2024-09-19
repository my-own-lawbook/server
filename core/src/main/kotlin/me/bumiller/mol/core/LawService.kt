package me.bumiller.mol.core

import me.bumiller.mol.core.data.LawContentService
import me.bumiller.mol.core.exception.ServiceException

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
     * @throws ServiceException.UserNotFound If the user could not be found
     * @throws ServiceException.LawBookNotFound If the book could not be found
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
     * @throws ServiceException.UserNotFound If the user could not be found
     * @throws ServiceException.LawEntryNotFound If the entry could not be found
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
     * @throws ServiceException.UserNotFound If the user could not be found
     * @throws ServiceException.LawSectionNotFound If the section could not be found
     */
    suspend fun isUserMemberOfSection(userId: Long, sectionId: Long): Boolean

}