package me.bumiller.mol.rest.validation

import kotlinx.datetime.Clock
import me.bumiller.mol.core.data.TwoFactorTokenService
import me.bumiller.mol.core.data.UserService
import me.bumiller.mol.model.TwoFactorToken
import me.bumiller.mol.model.TwoFactorTokenType
import me.bumiller.mol.model.http.conflictUnique
import me.bumiller.mol.model.http.notFoundIdentifier
import java.util.*

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

/**
 * Throws in the case that a [TwoFactorToken]:
 * - Does not exist
 * - Does not exist for the (optionally given) user
 * - Does not match the (optionally given) [TwoFactorTokenType]
 * - Has already been used
 * - Is expired
 *
 * @param twoFactorTokenService The token service
 * @param type The optional type to match
 */
internal suspend fun UUID.validateTwoFactorTokenValid(
    twoFactorTokenService: TwoFactorTokenService,
    type: TwoFactorTokenType? = null,
    userId: Long? = null
) {
    val now = Clock.System.now()

    val token = twoFactorTokenService.getSpecific(token = this) ?: notFoundIdentifier("two-factor-token", toString())

    val typeMatching = (type != null && token.type == type) || type == null
    val userMatching = (userId != null && token.user.id == userId) || userId == null
    val notExpired = token.expiringAt == null || (token.expiringAt!! > now)
    val notUsed = !token.used

    if (!typeMatching || !userMatching || !notExpired || !notUsed) notFoundIdentifier("two-factor-token", toString())
}