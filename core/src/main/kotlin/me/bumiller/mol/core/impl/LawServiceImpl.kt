package me.bumiller.mol.core.impl

import me.bumiller.mol.core.LawService
import me.bumiller.mol.core.data.LawContentService
import me.bumiller.mol.model.LawBook
import me.bumiller.mol.model.User

internal class LawServiceImpl(
    val lawContentService: LawContentService
) : LawService {

    override suspend fun isUserMemberOfBook(userId: Long, bookId: Long): Boolean =
        lawContentService
            .getSpecificBook(id = bookId)
            ?.containsUser(userId) ?: throw IllegalArgumentException("Can't find book for id '$bookId'")

    override suspend fun isUserMemberOfEntry(userId: Long, entryId: Long): Boolean =
        lawContentService
            .getBookByEntry(entryId)
            ?.containsUser(userId) ?: throw IllegalArgumentException("Can't find entry for id '$entryId'")

    override suspend fun isUserMemberOfSection(userId: Long, sectionId: Long): Boolean =
        lawContentService
            .getEntryForSection(sectionId)?.id
            ?.let { lawContentService.getBookByEntry(it) }
            ?.containsUser(userId) ?: throw IllegalArgumentException("Can't find section for id '$sectionId'")


    private fun LawBook.containsUser(id: Long) =
        id in members.map(User::id)

}