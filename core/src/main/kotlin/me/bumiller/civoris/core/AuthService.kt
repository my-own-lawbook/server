package me.bumiller.civoris.core

import me.bumiller.civoris.core.exception.ServiceException
import me.bumiller.civoris.model.AuthTokens
import me.bumiller.civoris.model.TwoFactorToken
import me.bumiller.civoris.model.User

/**
 * Interface to perform common authentication actions.
 */
interface AuthService {

    /**
     * Routine for signing up a new user.
     *
     * This will add a user to the database that does not have their email verified and no profile attached to them.
     *
     * @param email The email of the user
     * @param username The username of the user
     * @param password The unhashed password of the user
     * @param sendVerificationEmail Whether this should automatically trigger sending an email with an email-verification-token to [email] for the user to verify their email address. Shorthand for calling [sendEmailVerification].
     * @return The created user
     * @throws ServiceException.UserUsernameNotUnique If the username is not available
     * @throws ServiceException.UserEmailNotUnique If the email is not available
     */
    suspend fun createNewUser(
        email: String,
        username: String,
        password: String,
        sendVerificationEmail: Boolean = false
    ): User

    /**
     * Routine for sending an email-verification-token to an email.
     *
     * This will create a two-factor-token for the email and send it to [User.email].
     *
     * @param user The user, used for personalization and for email address
     * @return The two-factor-token that was created for the email.
     * @throws ServiceException.UserNotFound If the user could not be found
     */
    suspend fun sendEmailVerification(user: User): TwoFactorToken

    /**
     * Routine for getting a user for passed credentials.
     *
     * @param email The email passed by the user
     * @param username The username passed by the user
     * @param password The raw, unhashed password passed by the user
     * @return The user, if one was found for [email] or [username] that matched with [password], or null if none was found or could not be authenticated.
     * @throws IllegalArgumentException If both [email] and [username] are present
     */
    suspend fun getAuthenticatedUser(email: String? = null, username: String? = null, password: String): User?

    /**
     * Creates [AuthTokens] for a specified user.
     *
     * @param userId The id of the user
     * @return The auth tokens for the user
     * @throws ServiceException.UserNotFound If the user was not found
     */
    suspend fun loginUser(userId: Long): AuthTokens

    /**
     * Will deactivate the refresh [tokens] added to the user with [userId].
     *
     * @param userId The id of the user
     * @param tokens The refresh tokens to deactivate
     * @throws ServiceException.UserNotFound If the user was not found
     * @throws ServiceException.TwoFactorTokenNotFound If one of the [tokens] was not found
     */
    suspend fun logoutUser(userId: Long, vararg tokens: String)

    /**
     * Will log in a user based on a refresh token.
     *
     * @param token The refresh token
     * @return The login tokens
     * @throws ServiceException.TwoFactorTokenNotFound If the token for [token] could not be found
     * @throws ServiceException.InvalidTwoFactorTokenType If the token for [token] is not an email token
     * @throws ServiceException.TwoFactorTokenExpired If the token for [token] is already expired
     * @throws ServiceException.TwoFactorTokenUsed If the token for [token] is already used
     * @throws ServiceException.UserNotFound If the user for the token for [token] could not be found
     */
    suspend fun loginUserWithRefreshToken(token: String): AuthTokens

    /**
     * Will set a user to have their email validated based on the [token].
     * Will also update [TwoFactorToken.used]
     *
     * @param token The token submitted by a user
     * @return The user that has had their email verification status updated
     * @throws ServiceException.TwoFactorTokenNotFound If the token for [token] could not be found
     * @throws ServiceException.InvalidTwoFactorTokenType If the token for [token] is not an email token
     * @throws ServiceException.TwoFactorTokenExpired If the token for [token] is already expired
     * @throws ServiceException.TwoFactorTokenUsed If the token for [token] is already used
     * @throws ServiceException.EmailTokenUserAlreadyVerified If the user for the token for [token] already has their email verified
     * @throws ServiceException.UserNotFound If the user for the token for [token] could not be found
     */
    suspend fun validateEmailWithToken(token: String): User

}