package me.bumiller.mol.validation.impl

import me.bumiller.mol.core.data.LawContentService
import me.bumiller.mol.core.data.MemberContentService
import me.bumiller.mol.core.data.UserService
import me.bumiller.mol.core.exception.ServiceException
import me.bumiller.mol.model.LawBook
import me.bumiller.mol.model.MemberRole
import me.bumiller.mol.model.User
import me.bumiller.mol.model.http.RequestException
import me.bumiller.mol.model.http.internal
import me.bumiller.mol.model.http.notFoundIdentifier
import me.bumiller.mol.validation.AccessValidator
import me.bumiller.mol.validation.LawPermission
import me.bumiller.mol.validation.LawResourceScope

internal class ServiceAccessValidator(
    private val lawContentService: LawContentService,
    private val memberContentService: MemberContentService,
    private val userService: UserService
) : AccessValidator {

    override suspend fun hasAccess(
        scope: LawResourceScope,
        permission: LawPermission,
        resourceId: Long,
        userId: Long,
        throwOnRestricted: Boolean
    ): Boolean? {
        val user = try {
            userService.getSpecific(id = userId)
        } catch (e: ServiceException.UserNotFound) {
            internal()
        }

        val parentBook = getParentBook(scope, resourceId) ?: if (throwOnRestricted) throw RequestException(404, "")
        else return null

        val memberRole = try {
            memberContentService.getMemberRole(user.id, parentBook.id)
        } catch (e: ServiceException.UserNotMemberOfBook) {
            null
        }

        val requiredRole = when (scope) {
            LawResourceScope.Book -> {
                when (permission) {
                    LawPermission.Create -> MemberRole.Write
                    LawPermission.Edit -> MemberRole.Admin
                    LawPermission.Read -> MemberRole.Read
                }
            }

            LawResourceScope.Entry, LawResourceScope.Section -> when (permission) {
                LawPermission.Create -> MemberRole.Write
                LawPermission.Edit -> MemberRole.Update
                LawPermission.Read -> MemberRole.Read
            }
        }

        return if (memberRole == null) {
            if (throwOnRestricted) throw RequestException(404, "")
            else false
        } else {
            if (memberRole satisfies requiredRole) {
                true
            } else if (throwOnRestricted) {
                if (memberRole satisfies MemberRole.Read) throw RequestException(401, "")
                else throw RequestException(404, "")
            } else {
                false
            }
        }
    }

    private suspend fun getParentBook(
        scope: LawResourceScope,
        resourceId: Long
    ): LawBook? = try {
        when (scope) {
            LawResourceScope.Book -> {
                lawContentService.getSpecificBook(id = resourceId)
            }

            LawResourceScope.Entry -> {
                lawContentService.getBookByEntry(resourceId)
            }

            LawResourceScope.Section -> {
                val entry = lawContentService.getEntryForSection(resourceId)
                lawContentService.getBookByEntry(entry.id)
            }
        }
    } catch (e: ServiceException) {
        null
    }

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