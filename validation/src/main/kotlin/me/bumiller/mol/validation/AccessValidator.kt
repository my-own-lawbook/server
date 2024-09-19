package me.bumiller.mol.validation

import me.bumiller.mol.model.User
import me.bumiller.mol.model.http.RequestException

/**
 * Validator that helps to decide whether a user had access to a specific resource or not.
 */
interface AccessValidator {

    /**
     * Validates whether a user has read access to a book, i.e. whether a user is either creator or member of a book.
     *
     * Will throw a [RequestException] with 404 if the access is not available
     *
     * @param user The user
     * @param bookId The id of the book
     */
    suspend fun validateReadBook(user: User, bookId: Long)

    /**
     * Validates whether a user has read access to an entry, i.e. whether a user is either creator or member of the parent book.
     *
     * Will throw a [RequestException] with 404 if the access is not available
     *
     * @param user The user
     * @param entryId The id of the entry
     */
    suspend fun validateReadEntry(user: User, entryId: Long)

    /**
     * Validates whether a user has read access to a section, i.e. whether a user is either creator or member of the parent book.
     *
     * Will throw a [RequestException] with 404 if the access is not available
     *
     * @param user The user
     * @param sectionId The id of the entry
     */
    suspend fun validateReadSection(user: User, sectionId: Long)

    /**
     * Validates whether a user has write access to a book, i.e. whether a user is the creator of the book.
     *
     * Will throw a [RequestException] with 404 if the access is not available
     *
     * @param user The user
     * @param bookId The id of the entry
     */
    suspend fun validateWriteBook(user: User, bookId: Long)

    /**
     * Validates whether a user has write access to an entry, i.e. whether a user is the creator of the parent book.
     *
     * Will throw a [RequestException] with 404 if the access is not available
     *
     * @param user The user
     * @param entryId The id of the entry
     */
    suspend fun validateWriteEntry(user: User, entryId: Long)

    /**
     * Validates whether a user has write access to a section, i.e. whether a user is the creator of the parent book.
     *
     * Will throw a [RequestException] with 404 if the access is not available
     *
     * @param user The user
     * @param sectionId The id of the section
     */
    suspend fun validateWriteSection(user: User, sectionId: Long)

}