package me.bumiller.mol.core.impl

import me.bumiller.mol.core.data.MemberService
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

    override suspend fun getMembersInBook(bookId: Long): List<User>? =
        bookRepository.getSpecific(bookId)
            ?.members
            ?.map(::mapUser)

    override suspend fun addMemberToBook(bookId: Long, userId: Long): List<User>? {
        val book = bookRepository.getSpecific(bookId) ?: return null
        val user = userRepository.getSpecific(userId) ?: return null

        if (book.creator.id == user.id) return null
        if (user.id !in book.members.map(UserModel::id)) {
            val updatedModel = book.copy(
                members = book.members + user
            )
            bookRepository.update(updatedModel)
        }

        return bookRepository.getSpecific(bookId)
            ?.members
            ?.map(::mapUser)
    }

    override suspend fun removeMemberFromBook(bookId: Long, userId: Long): List<User>? {
        val book = bookRepository.getSpecific(bookId) ?: return null
        val user = userRepository.getSpecific(userId) ?: return null

        if (user.id in book.members.map(UserModel::id)) {
            val updatedModel = book.copy(
                members = book.members - user
            )
            bookRepository.update(updatedModel)
        }

        return bookRepository.getSpecific(bookId)
            ?.members
            ?.map(::mapUser)
    }

    override suspend fun getMemberRole(userId: Long, bookId: Long): MemberRole? =
        roleRepository
            .getMemberRole(userId, bookId)
            ?.let(::memberRoleFromString)

    override suspend fun setMemberRole(userId: Long, bookId: Long, role: MemberRole): Boolean {
        val user = userRepository.getSpecific(userId) ?: return false
        val book = bookRepository.getSpecific(bookId) ?: return false
        val isUserMember = user.id in book.members.map(UserModel::id)

        if (!isUserMember) return false

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

            if (!otherMemberIsAdmin) return false
        }

        roleRepository.setMemberRole(user.id, book.id, memberRoleToString(role))
        return true
    }
}