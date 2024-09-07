package me.bumiller.mol.rest.validation

import me.bumiller.mol.core.data.UserService
import me.bumiller.mol.model.http.conflictUnique

/**
 * Throws in the case that an email is already taken
 *
 * @param userService The user service
 */
internal suspend fun String.validateEmailUnique(userService: UserService) =
    userService.getSpecific(email = this)?.let {
        conflictUnique("email", this)
    }

/**
 * Throws in the case that a username is already taken
 *
 * @param userService The user service
 */
internal suspend fun String.validateUsernameUnique(userService: UserService) =
    userService.getSpecific(username = this)?.let {
        conflictUnique("username", this)
    }