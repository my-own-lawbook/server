package me.bumiller.mol.validation.actions

import me.bumiller.mol.model.http.notFoundIdentifier
import me.bumiller.mol.validation.ValidatableWrapper

/**
 * Validates that a user with a specific id exists
 */
suspend fun ValidatableWrapper<Long>.userExists() {
    scope.userService.getSpecific(id = value) ?: notFoundIdentifier("user", value.toString())
}