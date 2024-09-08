package me.bumiller.mol.core

import me.bumiller.mol.model.AuthTokens
import me.bumiller.mol.model.TwoFactorToken
import me.bumiller.mol.model.User
import me.bumiller.mol.model.http.RequestException
import java.util.*

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
     * @throws IllegalStateException When a user with [email] or [username] already exists
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
     * @throws IllegalStateException When the token could not be created
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
     * @throws IllegalArgumentException If a user for [userId] was not found
     */
    suspend fun loginUser(userId: Long): AuthTokens

    /**
     * Will deactivate the refresh [tokens] added to the user with [userId].
     *
     * @param userId The id of the user
     * @param tokens The refresh tokens to deactivate
     */
    suspend fun logoutUser(userId: Long, vararg tokens: UUID)

    /**
     * Will set a user to have their email validated based on the [tokenUUID].
     * Will also update [TwoFactorToken.used]
     *
     * Will fail (throw) if any of the following cases occurs:
     * - The token is not found/invalid/expired/already used
     * - No user could be found for the token
     * - The token is not an email-verification-token
     *
     * @param tokenUUID The UUID of the token submitted by a user
     * @return The user that has had their email verification status updated
     * @throws IllegalArgumentException If any of the above states occur
     */
    suspend fun validateEmailWithToken(tokenUUID: UUID): User

}