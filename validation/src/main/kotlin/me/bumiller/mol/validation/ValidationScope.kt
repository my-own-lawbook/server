package me.bumiller.mol.validation

import me.bumiller.mol.core.AuthService
import me.bumiller.mol.core.LawService
import me.bumiller.mol.core.data.LawContentService
import me.bumiller.mol.core.data.TwoFactorTokenService
import me.bumiller.mol.core.data.UserService

/**
 * Scope that validations are performed in. Provides access to the common services for use in contextual validation.
 */
interface ValidationScope {

    /**
     * The [TwoFactorTokenService]
     */
    val tokenService: TwoFactorTokenService

    /**
     * The [UserService]
     */
    val userService: UserService

    /**
     * The [AuthService]
     */
    val authService: AuthService

    /**
     * The [LawService]
     */
    val lawService: LawService

    /**
     * The [LawContentService]
     */
    val lawContentService: LawContentService

}