package me.bumiller.mol.core

import me.bumiller.mol.model.AuthTokens
import me.bumiller.mol.model.TwoFactorToken
import me.bumiller.mol.model.User
import me.bumiller.mol.model.http.RequestException
import java.util.*

/**
 * Interface to perform common authentication actions.
 *
 * Actions provided by this service will not signal failure by return value, but by throwing [RequestException]'s. This is done so to automatically respond to http-requests in the case of an error or wrong input value.
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
     */
    fun createNewUser(
        email: String,
        username: String,
        password: String,
        sendVerificationEmail: Boolean = false
    ): User

    /**
     * Routine for sending an email-verification-token to an email.
     *
     * This will create a two-factor-token for the email and send it to [email].
     *
     * @param email The email to send the token to. Also, the email referenced in the token
     * @return The two-factor-token that was created for the email.
     */
    fun sendEmailVerification(email: String): TwoFactorToken

    /**
     * Routine for getting a user for passed credentials.
     *
     * Either [username] or [email] need to be passed. Otherwise, [RequestException] with status 400 is thrown.
     *
     * @param email The email passed by the user
     * @param username The username passed by the user
     * @param password The raw, unhashed password passed by the user.
     * @return The user, if one was found for [email] or [username] that matched with [password].
     */
    fun getAuthenticatedUser(email: String? = null, username: String? = null, password: String): User

    /**
     * Creates [AuthTokens] for a specified user.
     *
     * **MUST** only be called after actually verifying the authentication of the user with [userId].
     *
     * @param userId The id of the user
     * @return The auth tokens for the user
     */
    fun loginUser(userId: Long): AuthTokens

    /**
     * Will deactivate the refresh [tokens] added to the user with [userId].
     *
     * @param userId The id of the user
     * @param tokens The refresh tokens to deactivate
     */
    fun logoutUser(userId: Long, vararg tokens: UUID)

}