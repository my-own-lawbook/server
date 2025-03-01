package me.bumiller.civoris.core.impl

import me.bumiller.civoris.core.LawService
import me.bumiller.civoris.core.data.LawContentService
import me.bumiller.civoris.model.LawBook
import me.bumiller.civoris.model.User

internal class LawServiceImpl(
    val lawContentService: LawContentService
) : LawService {

    override suspend fun isUserMemberOfBook(userId: Long, bookId: Long): Boolean =
        lawContentService
            .getSpecificBook(id = bookId)
            .containsUser(userId)

    override suspend fun isUserMemberOfEntry(userId: Long, entryId: Long): Boolean =
        lawContentService
            .getBookByEntry(entryId)
            .containsUser(userId)

    override suspend fun isUserMemberOfSection(userId: Long, sectionId: Long): Boolean =
        lawContentService
            .getEntryForSection(sectionId).id
            .let { lawContentService.getBookByEntry(it) }
            .containsUser(userId)


    private fun LawBook.containsUser(id: Long) =
        id in members.map(User::id)

}