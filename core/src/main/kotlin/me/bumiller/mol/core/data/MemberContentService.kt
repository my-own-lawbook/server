package me.bumiller.mol.core.data

import me.bumiller.mol.core.MemberService
import me.bumiller.mol.core.exception.ServiceException
import me.bumiller.mol.model.MemberRole
import me.bumiller.mol.model.User

/**
 * Service to access actions related to the members of specific law-books
 */
@SuppressWarnings("kotlin:S6526")
abstract class MemberContentService {

    /**
     * Gets all members of a specific book
     *
     * @param bookId The id of the book to query the members for
     * @return The list of members in the book
     * @throws ServiceException.LawBookNotFound If the book was not found
     */
    abstract suspend fun getMembersInBook(bookId: Long): List<User>

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
    abstract suspend fun getMemberRole(userId: Long, bookId: Long): MemberRole

    /**
     * Adds a member to a specific book.
     *
     * Does **not** perform business-logic checks. Use [MemberService.addMemberToBook] if possible!
     *
     * @param bookId The id of the book to add the member to
     * @param userId The id of the user to add to the book
     * @throws ServiceException.LawBookNotFound If the book was not found
     * @throws ServiceException.UserNotFound If the user was not found
     */
    internal abstract suspend fun addMemberToBook(bookId: Long, userId: Long)

    /**
     * Removes a member to a specific book
     *
     * Does **not** perform business-logic checks. Use [MemberService.removeMemberFromBook] if possible!
     *
     * @param bookId The id of the book to remove the member from
     * @param userId The id of the user to remove from the book
     * @return The list of members in the book after the update
     * @throws ServiceException.LawBookNotFound If the book could not be found
     * @throws ServiceException.UserNotFound If the user could not be found
     */
    internal abstract suspend fun removeMemberFromBook(bookId: Long, userId: Long)

    /**
     * Sets the book-wide role of a member
     *
     * Does **not** perform business-logic checks. Use [MemberService.setMemberRole] if possible!
     *
     * @param userId The id of the user
     * @param bookId The id of the book
     * @param role The new role of the member
     * @throws ServiceException.UserNotFound If the user could not be found
     * @throws ServiceException.LawBookNotFound If the book could not be found
     */
    internal abstract suspend fun setMemberRole(userId: Long, bookId: Long, role: MemberRole)

}