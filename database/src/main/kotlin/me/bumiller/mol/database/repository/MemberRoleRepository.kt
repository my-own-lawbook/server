package me.bumiller.mol.database.repository

import me.bumiller.mol.database.table.crossref.LawBookMembersCrossref
import me.bumiller.mol.database.util.suspendTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and

/**
 * Interface to access the book-wide roles of a member in a book
 */
interface MemberRoleRepository {

    /**
     * Gets the role of a user in a book
     *
     * @param userId The id of the user
     * @param bookId The id of the book
     * @return The role string, or null if it was not found
     */
    suspend fun getMemberRole(userId: Long, bookId: Long): String?

    /**
     * Sets the role of a user in a book
     *
     * @param userId The id of the user
     * @param bookId The id of the book
     * @param role The new role of the user
     */
    suspend fun setMemberRole(userId: Long, bookId: Long, role: String)

}

internal class ExposedMemberRoleRepository : MemberRoleRepository {

    override suspend fun getMemberRole(userId: Long, bookId: Long): String? = suspendTransaction {
        LawBookMembersCrossref.Entity.find {
            (LawBookMembersCrossref.Table.member eq userId) and (LawBookMembersCrossref.Table.lawBook eq bookId)
        }
            .limit(1)
            .firstOrNull()?.role
    }

    override suspend fun setMemberRole(userId: Long, bookId: Long, role: String) = suspendTransaction {
        LawBookMembersCrossref.Entity.findSingleByAndUpdate(
            op = (LawBookMembersCrossref.Table.member eq userId) and (LawBookMembersCrossref.Table.lawBook eq bookId)
        ) {
            it.role = role
        }
        Unit
    }

}