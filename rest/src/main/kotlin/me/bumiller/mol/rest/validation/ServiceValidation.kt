package me.bumiller.mol.rest.validation

import kotlinx.datetime.Clock
import me.bumiller.mol.common.present
import me.bumiller.mol.model.TwoFactorToken
import me.bumiller.mol.model.TwoFactorTokenType
import me.bumiller.mol.model.User
import me.bumiller.mol.model.http.conflictUnique
import me.bumiller.mol.model.http.notFoundIdentifier
import java.util.*

/**
 * Throws in the case that an email is already taken
 */
internal suspend fun ValidatableWrapper<String>.isEmailUnique() =
    scope.userService.getSpecific(email = value)?.let {
        conflictUnique("email", value)
    }

/**
 * Throws in the case that a username is already taken
 */
internal suspend fun ValidatableWrapper<String>.isUsernameUnique() =
    scope.userService.getSpecific(username = value)?.let {
        conflictUnique("username", value)
    }

/**
 * Throws in the case that a [TwoFactorToken]:
 * - Does not exist
 * - Does not exist for the (optionally given) user
 * - Does not match the (optionally given) [TwoFactorTokenType]
 * - Has already been used
 * - Is expired
 *
 * @param type The optional type to match
 */
internal suspend fun ValidatableWrapper<UUID>.isTokenValid(
    type: TwoFactorTokenType? = null,
    userId: Long? = null
) {
    val now = Clock.System.now()

    val token = scope.tokenService.getSpecific(token = value) ?: notFoundIdentifier("two-factor-token", toString())

    val typeMatching = (type != null && token.type == type) || type == null
    val userMatching = (userId != null && token.user.id == userId) || userId == null
    val notExpired = token.expiringAt == null || (token.expiringAt!! > now)
    val notUsed = !token.used

    if (!typeMatching || !userMatching || !notExpired || !notUsed) notFoundIdentifier("two-factor-token", toString())
}

/**
 * Throws a 404 in the case that a user is not a member or creator of all the provided law resources.
 *
 * @param lawBookId The id of the law-book
 * @param lawEntryId The id of the law-entry
 * @param lawSectionId The id of the law section
 */
internal suspend fun ValidatableWrapper<User>.hasReadAccess(
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
            val entry = scope.lawContentService.getEntryForSection(lawSectionId)!!
            val book = scope.lawContentService.getBookByEntry(entry.id)!!

            val valid = book.creator.id == value.id || scope.lawService.isUserMemberOfSection(value.id, lawSectionId)
            if (!valid) notFoundIdentifier("law-section", lawSectionId.toString())
        }

        lawEntryId != null -> {
            val book = scope.lawContentService.getBookByEntry(lawEntryId)!!

            val valid = book.creator.id == value.id || scope.lawService.isUserMemberOfEntry(value.id, lawEntryId)
            if (!valid) notFoundIdentifier("law-entry", lawEntryId.toString())
        }

        lawBookId != null -> {
            val book = scope.lawContentService.getSpecificBook(id = lawBookId)!!

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
internal suspend fun ValidatableWrapper<User>.hasWriteAccess(
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
            val entry = scope.lawContentService.getEntryForSection(lawSectionId)!!
            val book = scope.lawContentService.getBookByEntry(entry.id)!!

            val valid = book.creator.id == value.id
            if (!valid) notFoundIdentifier("law-section", lawSectionId.toString())
        }

        lawEntryId != null -> {
            val book = scope.lawContentService.getBookByEntry(lawEntryId)!!

            val valid = book.creator.id == value.id
            if (!valid) notFoundIdentifier("law-entry", lawEntryId.toString())
        }

        lawBookId != null -> {
            val book = scope.lawContentService.getSpecificBook(id = lawBookId)!!

            val valid = book.creator.id == value.id
            if (!valid) notFoundIdentifier("law-book", lawBookId.toString())
        }
    }
}

/**
 * Throws a 409 in the case that a law-book with the key already exists
 */
internal suspend fun ValidatableWrapper<String>.isUniqueBookKey() {
    scope.lawContentService.getSpecificBook(key = value)?.let {
        conflictUnique("key", value)
    }
}

/**
 * Throws a 409 in the case that a law-entry with the key already exists
 */
internal suspend fun ValidatableWrapper<String>.isUniqueEntryKey() {
    scope.lawContentService.getSpecificEntry(key = present(value))?.let {
        conflictUnique("key", value)
    }
}