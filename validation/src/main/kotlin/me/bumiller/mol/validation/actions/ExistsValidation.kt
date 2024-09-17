package me.bumiller.mol.validation.actions

import me.bumiller.mol.model.http.notFoundIdentifier
import me.bumiller.mol.validation.ValidatableWrapper

/**
 * Validates that a user with a specific id exists.
 *
 * @param requireEmailAndProfile Whether to also require that the given user has the email verified and a profile set. Will still respond with 404.
 */
suspend fun ValidatableWrapper<Long>.userExists(requireEmailAndProfile: Boolean = false) {
    val user = scope.userService.getSpecific(id = value) ?: notFoundIdentifier("user", value.toString())

    if (requireEmailAndProfile && (!user.isEmailVerified || user.profile == null))
        notFoundIdentifier("user", value.toString())
}