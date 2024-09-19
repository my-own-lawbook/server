package me.bumiller.mol.core.data

import me.bumiller.mol.core.exception.ServiceException
import me.bumiller.mol.model.MemberRole
import me.bumiller.mol.model.User

/**
 * Service to access actions related to the members of specific law-books
 */
interface MemberContentService {

    /**
     * Gets all members of a specific book
     *
     * @param bookId The id of the book to query the members for
     * @return The list of members in the book
     * @throws ServiceException.LawBookNotFound If the book was not found
     */
    suspend fun getMembersInBook(bookId: Long): List<User>

    /**
     * Adds a member to a specific book
     *
     * @param bookId The id of the book to add the member to
     * @param userId The id of the user to add to the book
     * @return The list of members in the book after the update
     * @throws ServiceException.LawBookNotFound If the book was not found
     * @throws ServiceException.UserNotFound If the user was not found
     * @throws ServiceException.CreatorTriedAddedToBook If the user with [userId] is the creator of the book
     * @throws ServiceException.UserAlreadyMemberOfBook If is already a member of the book
     */
    suspend fun addMemberToBook(bookId: Long, userId: Long): List<User>

    /**
     * Removes a member to a specific book
     *
     * @param bookId The id of the book to remove the member from
     * @param userId The id of the user to remove from the book
     * @return The list of members in the book after the update
     * @throws ServiceException.LawBookNotFound If the book could not be found
     * @throws ServiceException.UserNotFound If the user could not be found
     * @throws ServiceException.UserNotMemberOfBook If the user is not a member of the book
     */
    suspend fun removeMemberFromBook(bookId: Long, userId: Long): List<User>

    /**
     * Gets the book-wide role of a member
     *
     * @param userId The id of the user
     * @param bookId The id of the book
     * @return The role for the user in the book
     * @throws ServiceException.UserNotFound If the user could not be found
     * @throws ServiceException.LawBookNotFound If the book could not be found
     * @throws ServiceException.UserNotMemberOfBook If the user is not a member of the book
     */
    suspend fun getMemberRole(userId: Long, bookId: Long): MemberRole

    /**
     * Sets the book-wide role of a member
     *
     * @param userId The id of the user
     * @param bookId The id of the book
     * @param role The new role of the member
     * @throws ServiceException.UserNotFound If the user could not be found
     * @throws ServiceException.LawBookNotFound If the book could not be found
     * @throws ServiceException.UserNotMemberOfBook If the user is not a member of the book
     * @throws ServiceException.BookNoAdminLeft If the book would not have an admin left after the operation
     */
    suspend fun setMemberRole(userId: Long, bookId: Long, role: MemberRole)

}