package me.bumiller.mol.core.impl

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import me.bumiller.mol.common.isNotEmpty
import me.bumiller.mol.core.AuthService
import me.bumiller.mol.core.EncryptionService
import me.bumiller.mol.core.data.TwoFactorTokenService
import me.bumiller.mol.core.data.UserService
import me.bumiller.mol.email.EmailService
import me.bumiller.mol.model.AuthTokens
import me.bumiller.mol.model.TwoFactorToken
import me.bumiller.mol.model.TwoFactorTokenType
import me.bumiller.mol.model.User
import java.util.*
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

internal class AuthServiceImpl(
    val userService: UserService,
    val tokenService: TwoFactorTokenService,
    val encryptor: EncryptionService,
    val emailService: EmailService
) : AuthService {

    // TODO: Move these properties to a configuration file
    companion object {

        val EMAIL_VERIFY_TOKEN_DURATION = 5.minutes

        val REFRESH_TOKEN_DURATION = 10.days

        val JWT_SIGNING_SECRET = "secret".repeat(10)

    }

    override suspend fun createNewUser(
        email: String,
        username: String,
        password: String,
        sendVerificationEmail: Boolean
    ): User {
        userService.run {
            listOf(
                getSpecific(email = email),
                getSpecific(username = username)
            )
        }.isNotEmpty {
            throw IllegalStateException("Found users for '$email' or '$username' already existing.'")
        }

        val user = userService.createUser(email, encryptor.encrypt(password), username)

        if (sendVerificationEmail)
            sendEmailVerification(user)

        return user
    }

    override suspend fun sendEmailVerification(user: User): TwoFactorToken {
        val now = Clock.System.now()
        val emailToken = tokenService.create(
            type = TwoFactorTokenType.EmailConfirm,
            userId = user.id,
            expiringAt = now.plus(EMAIL_VERIFY_TOKEN_DURATION),
            issuedAt = now,
            additionalContent = user.email
        ) ?: throw IllegalStateException("Could not create an email-verification-token for the user.")

        emailService.sendEmailVerifyEmail(user, emailToken)

        return emailToken
    }

    override suspend fun getAuthenticatedUser(email: String?, username: String?, password: String): User? {
        val validArgs = listOfNotNull(email, username).size == 1
        if (!validArgs) throw IllegalArgumentException("Both email and username were passed.")

        val user = userService.getSpecific(email = email, username = username)

        val authenticated = if (user != null) {
            encryptor.verify(password, user.password)
        } else false

        return if (authenticated) return user!!
        else null
    }

    override suspend fun loginUser(userId: Long): AuthTokens {
        val now = Clock.System.now()
        val expiringAt = now.plus(REFRESH_TOKEN_DURATION)

        val user = userService.getSpecific(id = userId)
            ?: throw IllegalArgumentException("Did not find a user for id '$userId'.")

        val refreshToken = tokenService.create(
            type = TwoFactorTokenType.RefreshToken,
            userId = userId,
            expiringAt = expiringAt,
            issuedAt = now
        ) ?: throw IllegalStateException("Could not create an email-verification-token for the user.")

        val jwt = JWT.create()
            .withExpiresAt(expiringAt.toJavaInstant())
            .withIssuedAt(now.toJavaInstant())
            .withSubject(user.email)
            .sign(Algorithm.HMAC256(JWT_SIGNING_SECRET))

        return AuthTokens(jwt, refreshToken)
    }

    override suspend fun logoutUser(userId: Long, vararg tokens: UUID) {
        val user = userService.getSpecific(id = userId)
            ?: throw IllegalArgumentException("Did not find a user for id '$userId'.")

        tokens.forEach { token ->
            val tokenModel = tokenService.getSpecific(token = token)
            if (tokenModel?.user?.id == user.id)
                tokenService.markAsUsed(tokenModel.id)
        }
    }
}