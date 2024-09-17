package me.bumiller.mol.validation.actions

import me.bumiller.mol.model.User
import me.bumiller.mol.model.http.notFoundIdentifier
import me.bumiller.mol.validation.ValidatableWrapper

/**
 * Throws a 404 in the case that a user is not a member or creator of all the provided law resources.
 *
 * @param lawBookId The id of the law-book
 * @param lawEntryId The id of the law-entry
 * @param lawSectionId The id of the law section
 */
suspend fun ValidatableWrapper<User>.hasReadAccess(
    lawBookId: Long? = null,
    lawEntryId: Long? = null,
    lawSectionId: Long? = null
) {
    require(
        listOfNotNull(
            lawBookId,
            lawEntryId,
            lawSectionId
        ).isNotEmpty()
    ) { "The id of at least one law resource must be passed." }

    when {
        lawSectionId != null -> {
            val entry = scope.lawContentService.getEntryForSection(lawSectionId) ?: notFoundIdentifier(
                "law-section",
                lawSectionId.toString()
            )
            val book = scope.lawContentService.getBookByEntry(entry.id) ?: notFoundIdentifier(
                "law-section",
                lawSectionId.toString()
            )

            val valid = book.creator.id == value.id || scope.lawService.isUserMemberOfSection(value.id, lawSectionId)
            if (!valid) notFoundIdentifier("law-section", lawSectionId.toString())
        }

        lawEntryId != null -> {
            val book = scope.lawContentService.getBookByEntry(lawEntryId) ?: notFoundIdentifier(
                "law-entry",
                lawEntryId.toString()
            )

            val valid = book.creator.id == value.id || scope.lawService.isUserMemberOfEntry(value.id, lawEntryId)
            if (!valid) notFoundIdentifier("law-entry", lawEntryId.toString())
        }

        lawBookId != null -> {
            val book = scope.lawContentService.getSpecificBook(id = lawBookId) ?: notFoundIdentifier(
                "law-book",
                lawBookId.toString()
            )

            val valid = book.creator.id == value.id || value.id in book.members.map(User::id)
            if (!valid) notFoundIdentifier("law-book", lawBookId.toString())
        }
    }
}

/**
 * Throws a 401 in the case that a user is not the creator of all the provided law resources.
 *
 * @param lawBookId The id of the law-book
 * @param lawEntryId The id of the law-entry
 * @param lawSectionId The id of the law section
 */
suspend fun ValidatableWrapper<User>.hasWriteAccess(
    lawBookId: Long? = null,
    lawEntryId: Long? = null,
    lawSectionId: Long? = null
) {
    require(
        listOfNotNull(
            lawBookId,
            lawEntryId,
            lawSectionId
        ).isNotEmpty()
    ) { "The id of at least one law resource must be passed." }

    when {
        lawSectionId != null -> {
            val entry = scope.lawContentService.getEntryForSection(lawSectionId) ?: notFoundIdentifier(
                "law-section",
                lawSectionId.toString()
            )
            val book = scope.lawContentService.getBookByEntry(entry.id) ?: notFoundIdentifier(
                "law-section",
                lawSectionId.toString()
            )

            val valid = book.creator.id == value.id
            if (!valid) notFoundIdentifier("law-section", lawSectionId.toString())
        }

        lawEntryId != null -> {
            val book = scope.lawContentService.getBookByEntry(lawEntryId) ?: notFoundIdentifier(
                "law-entry",
                lawEntryId.toString()
            )

            val valid = book.creator.id == value.id
            if (!valid) notFoundIdentifier("law-entry", lawEntryId.toString())
        }

        lawBookId != null -> {
            val book = scope.lawContentService.getSpecificBook(id = lawBookId) ?: notFoundIdentifier(
                "law-book",
                lawBookId.toString()
            )

            val valid = book.creator.id == value.id
            if (!valid) notFoundIdentifier("law-book", lawBookId.toString())
        }
    }
}