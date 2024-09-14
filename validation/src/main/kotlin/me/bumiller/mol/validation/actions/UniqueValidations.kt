package me.bumiller.mol.validation.actions

import me.bumiller.mol.common.present
import me.bumiller.mol.model.http.conflictUnique
import me.bumiller.mol.validation.ValidatableWrapper

/**
 * Throws in the case that an email is already taken
 */
suspend fun ValidatableWrapper<String>.isEmailUnique() =
    scope.userService.getSpecific(email = value)?.let {
        conflictUnique("email", value)
    }

/**
 * Throws in the case that a username is already taken
 */
suspend fun ValidatableWrapper<String>.isUsernameUnique() =
    scope.userService.getSpecific(username = value)?.let {
        conflictUnique("username", value)
    }

/**
 * Throws a 409 in the case that a law-book with the key already exists
 */
suspend fun ValidatableWrapper<String>.isUniqueBookKey() {
    scope.lawContentService.getSpecificBook(key = value)?.let {
        conflictUnique("key", value)
    }
}

/**
 * Throws a 409 in the case that a law-entry with the key already exists in the same book
 */
suspend fun ValidatableWrapper<String>.isUniqueEntryKey(bookId: Long) {
    scope.lawContentService.getSpecificEntry(
        key = present(value),
        parentBookId = present(bookId)
    )?.let {
        conflictUnique("key", value)
    }
}

/**
 * Throws a 409 in the case that a law-section with the index already exists in the same entry
 */
suspend fun ValidatableWrapper<String>.isUniqueSectionIndex(entryKey: Long) {
    scope.lawContentService.getSpecificSection(
        index = present(value),
        parentEntryId = present(entryKey)
    )?.let {
        conflictUnique("index", value)
    }
}