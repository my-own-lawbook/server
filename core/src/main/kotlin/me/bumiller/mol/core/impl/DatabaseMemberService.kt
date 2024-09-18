package me.bumiller.mol.core.impl

import me.bumiller.mol.core.data.MemberService
import me.bumiller.mol.core.exception.ServiceException
import me.bumiller.mol.core.mapping.mapUser
import me.bumiller.mol.core.mapping.memberRoleFromString
import me.bumiller.mol.core.mapping.memberRoleToString
import me.bumiller.mol.database.repository.LawBookRepository
import me.bumiller.mol.database.repository.MemberRoleRepository
import me.bumiller.mol.database.repository.UserRepository
import me.bumiller.mol.model.MemberRole
import me.bumiller.mol.model.User
import me.bumiller.mol.database.table.User.Model as UserModel

internal class DatabaseMemberService(
    private val bookRepository: LawBookRepository,
    private val userRepository: UserRepository,
    private val roleRepository: MemberRoleRepository
) : MemberService {

    override suspend fun getMembersInBook(bookId: Long): List<User> =
        bookRepository.getSpecific(bookId)
            ?.members?.map(::mapUser) ?: throw ServiceException.LawBookNotFound(id = bookId)

    override suspend fun addMemberToBook(bookId: Long, userId: Long): List<User> {
        val book = bookRepository.getSpecific(bookId) ?: throw ServiceException.LawBookNotFound(id = bookId)
        val user = userRepository.getSpecific(userId) ?: throw ServiceException.UserNotFound(id = userId)

        if (book.creator.id == user.id) throw ServiceException.CreatorTriedAddedToBook

        if (user.id in book.members.map(UserModel::id)) throw ServiceException.UserAlreadyMemberOfBook(userId, bookId)

        val updatedModel = book.copy(
            members = book.members + user
        )
        bookRepository.update(updatedModel)

        return bookRepository.getSpecific(bookId)!!
            .members
            .map(::mapUser)
    }

    override suspend fun removeMemberFromBook(bookId: Long, userId: Long): List<User> {
        val book = bookRepository.getSpecific(bookId) ?: throw ServiceException.LawBookNotFound(id = bookId)
        val user = userRepository.getSpecific(userId) ?: throw ServiceException.UserNotFound(id = userId)

        if (user.id !in book.members.map(UserModel::id)) throw ServiceException.UserNotMemberOfBook(userId, bookId)

        val updatedModel = book.copy(
            members = book.members - user
        )
        bookRepository.update(updatedModel)

        return bookRepository.getSpecific(bookId)!!
            .members
            .map(::mapUser)
    }

    override suspend fun getMemberRole(userId: Long, bookId: Long): MemberRole {
        userRepository.getSpecific(userId) ?: throw ServiceException.UserNotFound(id = userId)
        val book = bookRepository.getSpecific(bookId) ?: throw ServiceException.LawBookNotFound(id = bookId)

        if (userId !in book.members.map(UserModel::id)) throw ServiceException.UserNotMemberOfBook(userId, bookId)

        return roleRepository
            .getMemberRole(userId, bookId)!!
            .let(::memberRoleFromString)
    }

    override suspend fun setMemberRole(userId: Long, bookId: Long, role: MemberRole) {
        val user = userRepository.getSpecific(userId) ?: throw ServiceException.UserNotFound(id = userId)
        val book = bookRepository.getSpecific(bookId) ?: throw ServiceException.LawBookNotFound(id = bookId)
        val isUserMember = user.id in book.members.map(UserModel::id)

        if (!isUserMember) throw ServiceException.UserNotMemberOfBook(userId, bookId)

        val currentRole = roleRepository.getMemberRole(user.id, book.id)!!.let(::memberRoleFromString)

        if (currentRole == MemberRole.Admin && role != MemberRole.Admin) {
            /* User is an admin and would be downgraded.
             * The scenario that no member in a book is an admin is disallowed,
             * so we need to check if any other member has the admin role
            */

            val otherMemberIsAdmin = book.members.any { member ->
                if (member.id == user.id) false
                else {
                    val roleOfMember = roleRepository.getMemberRole(member.id, book.id)!!.let(::memberRoleFromString)
                    roleOfMember == MemberRole.Admin
                }
            }

            if (!otherMemberIsAdmin) throw ServiceException.BookNoAdminLeft(bookId)
        }

        roleRepository.setMemberRole(user.id, book.id, memberRoleToString(role))
    }
}