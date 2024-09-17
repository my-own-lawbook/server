package me.bumiller.mol.core.data

import me.bumiller.mol.model.User

/**
 * Service to access actions related to the members of specific law-books
 */
interface MemberService {

    /**
     * Gets all members of a specific book
     *
     * @param bookId The id of the book to query the members for
     * @return The list of members in the book, or null if it was not found
     */
    suspend fun getMembersInBook(bookId: Long): List<User>?

    /**
     * Adds a member to a specific book
     *
     * @param bookId The id of the book to add the member to
     * @param userId The id of the user to add to the book
     * @return The list of members in the book after the update, or null if the user or book was not found, or if the user is the creator of the book.
     */
    suspend fun addMemberToBook(bookId: Long, userId: Long): List<User>?

    /**
     * Removes a member to a specific book
     *
     * @param bookId The id of the book to remove the member from
     * @param userId The id of the user to remove from the book
     * @return The list of members in the book after the update, or null if the user or book was not found
     */
    suspend fun removeMemberFromBook(bookId: Long, userId: Long): List<User>?

}