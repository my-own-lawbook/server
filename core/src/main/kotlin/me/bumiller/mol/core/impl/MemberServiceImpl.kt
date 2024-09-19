package me.bumiller.mol.core.impl

import me.bumiller.mol.core.MemberService
import me.bumiller.mol.core.data.LawContentService
import me.bumiller.mol.core.data.MemberContentService
import me.bumiller.mol.core.exception.ServiceException
import me.bumiller.mol.model.LawBook
import me.bumiller.mol.model.MemberRole
import me.bumiller.mol.model.User

internal class MemberServiceImpl(
    private val memberContentService: MemberContentService,
    private val lawContentService: LawContentService
) : MemberService {

    override suspend fun addMemberToBook(bookId: Long, userId: Long): List<User> {
        val book = lawContentService.getSpecificBook(id = bookId)
        val membersInBook = memberContentService.getMembersInBook(book.id)

        if (book.creator.id == userId) throw ServiceException.CreatorTriedAddedToBook

        if (userId in membersInBook.map(User::id)) throw ServiceException.UserAlreadyMemberOfBook(userId, bookId)

        memberContentService.addMemberToBook(bookId, userId)

        return memberContentService.getMembersInBook(bookId)
    }

    override suspend fun removeMemberFromBook(bookId: Long, userId: Long): List<User> {
        ensureUserInBookAndOtherAdmin(bookId, userId)

        memberContentService.removeMemberFromBook(bookId, userId)

        return memberContentService.getMembersInBook(bookId)
    }

    override suspend fun setMemberRole(userId: Long, bookId: Long, role: MemberRole) {
        ensureUserInBookAndOtherAdmin(bookId, userId)

        memberContentService.setMemberRole(bookId, userId, role)
    }

    // Throws the matching exception if the user is either not part of the book or no other admin is present
    private suspend fun ensureUserInBookAndOtherAdmin(bookId: Long, userId: Long) {
        val membersInBook = memberContentService.getMembersInBook(bookId)
        val book = lawContentService.getSpecificBook(id = bookId)

        if (userId !in membersInBook.map(User::id)) throw ServiceException.UserNotMemberOfBook(userId, bookId)

        if (!book.hasAdminApartFrom(userId)) throw ServiceException.BookNoAdminLeft(book.id)
    }

    // Checks whether an admin apart from [allowedAdmins] is present
    private suspend fun LawBook.hasAdminApartFrom(vararg allowedAdmins: Long) = members.any { member ->
        if (member.id in allowedAdmins) false
        else {
            val memberRole = memberContentService.getMemberRole(member.id, id)
            memberRole == MemberRole.Admin
        }
    }

}