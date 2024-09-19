package me.bumiller.mol.core

import me.bumiller.mol.core.exception.ServiceException
import me.bumiller.mol.model.MemberRole
import me.bumiller.mol.model.User

/**
 * Service to perform actions on the members of books.
 */
interface MemberService {

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
     * @throws ServiceException.BookNoAdminLeft If no admin was left in the book afterward
     */
    suspend fun removeMemberFromBook(bookId: Long, userId: Long): List<User>

    /**
     * Sets the book-wide role of a member
     *
     * Does **not** perform business-logic checks. Use [MemberService.removeMemberFromBook] if possible!
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