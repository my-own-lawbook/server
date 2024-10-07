package me.bumiller.mol.core.impl

import me.bumiller.mol.core.data.MemberContentService
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

internal class DatabaseMemberContentService(
    private val bookRepository: LawBookRepository,
    private val userRepository: UserRepository,
    private val roleRepository: MemberRoleRepository
) : MemberContentService() {

    override suspend fun getMembersInBook(bookId: Long): List<User> =
        bookRepository.getSpecific(bookId)
            ?.members?.map(::mapUser) ?: throw ServiceException.LawBookNotFound(id = bookId)

    override suspend fun addMemberToBook(bookId: Long, userId: Long) {
        val book = bookRepository.getSpecific(bookId) ?: throw ServiceException.LawBookNotFound(id = bookId)
        val user = userRepository.getSpecific(userId) ?: throw ServiceException.UserNotFound(id = userId)

        val updatedModel = book.copy(
            members = book.members + user
        )
        bookRepository.update(updatedModel)
    }

    override suspend fun removeMemberFromBook(bookId: Long, userId: Long) {
        val book = bookRepository.getSpecific(bookId) ?: throw ServiceException.LawBookNotFound(id = bookId)
        val user = userRepository.getSpecific(userId) ?: throw ServiceException.UserNotFound(id = userId)

        val updatedModel = book.copy(
            members = book.members - user
        )
        bookRepository.update(updatedModel)
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

        roleRepository.setMemberRole(user.id, book.id, memberRoleToString(role))
    }
}