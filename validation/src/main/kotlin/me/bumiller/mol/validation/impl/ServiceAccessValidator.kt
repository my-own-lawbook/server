package me.bumiller.mol.validation.impl

import me.bumiller.mol.core.data.LawContentService
import me.bumiller.mol.core.exception.ServiceException
import me.bumiller.mol.model.User
import me.bumiller.mol.model.http.notFoundIdentifier
import me.bumiller.mol.validation.AccessValidator

internal class ServiceAccessValidator(
    private val lawContentService: LawContentService
) : AccessValidator {

    override suspend fun validateReadBook(user: User, bookId: Long) {
        bookAccessLevel(user.id, bookId).requireRead("law-book", bookId.toString())
    }

    override suspend fun validateReadEntry(user: User, entryId: Long) {
        entryAccessLevel(user.id, entryId).requireRead("law-entry", entryId.toString())
    }

    override suspend fun validateReadSection(user: User, sectionId: Long) {
        sectionAccessLevel(user.id, sectionId).requireRead("law-section", sectionId.toString())
    }

    override suspend fun validateWriteBook(user: User, bookId: Long) {
        bookAccessLevel(user.id, bookId).requireWrite("law-book", bookId.toString())
    }

    override suspend fun validateWriteEntry(user: User, entryId: Long) {
        entryAccessLevel(user.id, entryId).requireWrite("law-entry", entryId.toString())
    }

    override suspend fun validateWriteSection(user: User, sectionId: Long) {
        sectionAccessLevel(user.id, sectionId).requireWrite("law-section", sectionId.toString())
    }


    // True for write, false for read, null for not at all
    private suspend fun bookAccessLevel(userId: Long, bookId: Long): Boolean? {
        val book = try {
            lawContentService.getSpecificBook(id = bookId)
        } catch (e: ServiceException) {
            null
        }

        val isCreator = book?.creator?.id == userId
        val isMember = userId in (book?.members?.map(User::id) ?: emptyList())

        return if (isCreator) true
        else if (isMember) false
        else null
    }

    private suspend fun entryAccessLevel(userId: Long, entryId: Long): Boolean? {
        val book = try {
            lawContentService.getBookByEntry(entryId)
        } catch (e: ServiceException) {
            null
        }

        return book?.id?.let { bookAccessLevel(userId, it) }
    }

    private suspend fun sectionAccessLevel(userId: Long, sectionId: Long): Boolean? {
        val entry = try {
            lawContentService.getEntryForSection(sectionId)
        } catch (e: ServiceException) {
            null
        }

        return entry?.id?.let { entryAccessLevel(userId, it) }
    }

    private fun Boolean?.requireRead(resourceName: String, identifier: String) {
        if (this == null) notFoundIdentifier(resourceName, identifier)
    }

    private fun Boolean?.requireWrite(resourceName: String, identifier: String) {
        if (this == null || !this) notFoundIdentifier(resourceName, identifier)
    }

}