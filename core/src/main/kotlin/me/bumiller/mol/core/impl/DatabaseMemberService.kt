package me.bumiller.mol.core.impl

import me.bumiller.mol.core.data.MemberService
import me.bumiller.mol.core.mapping.mapUser
import me.bumiller.mol.database.repository.LawBookRepository
import me.bumiller.mol.database.repository.UserRepository
import me.bumiller.mol.model.User
import me.bumiller.mol.database.table.User.Model as UserModel

internal class DatabaseMemberService(
    private val bookRepository: LawBookRepository,
    private val userRepository: UserRepository
) : MemberService {

    override suspend fun getMembersInBook(bookId: Long): List<User>? =
        bookRepository.getSpecific(bookId)
            ?.members
            ?.map(::mapUser)

    override suspend fun addMemberToBook(bookId: Long, userId: Long): List<User>? {
        val book = bookRepository.getSpecific() ?: return null
        val user = userRepository.getSpecific(userId) ?: return null

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
        val book = bookRepository.getSpecific() ?: return null
        val user = userRepository.getSpecific(userId) ?: return null

        if (user.id !in book.members.map(UserModel::id)) {
            val updatedModel = book.copy(
                members = book.members - user
            )
            bookRepository.update(updatedModel)
        }

        return bookRepository.getSpecific(bookId)
            ?.members
            ?.map(::mapUser)
    }
}