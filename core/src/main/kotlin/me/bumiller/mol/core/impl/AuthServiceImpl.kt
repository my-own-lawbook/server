package me.bumiller.mol.core.impl

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import me.bumiller.mol.core.AuthService
import me.bumiller.mol.core.EncryptionService
import me.bumiller.mol.core.data.TwoFactorTokenService
import me.bumiller.mol.core.data.UserService
import me.bumiller.mol.email.EmailService
import me.bumiller.mol.model.AuthTokens
import me.bumiller.mol.model.TwoFactorToken
import me.bumiller.mol.model.TwoFactorTokenType
import me.bumiller.mol.model.User
import me.bumiller.mol.model.http.*
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
        userService.getSpecific(email = email) ?: conflictUnique("email", email)
        userService.getSpecific(username = username) ?: conflictUnique("username", username)

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
        ) ?: notFoundIdentifier("user", user.id.toString())

        emailService.sendEmailVerifyEmail(user, emailToken)

        return emailToken
    }

    override suspend fun getAuthenticatedUser(email: String?, username: String?, password: String): User {
        val validArgs = listOfNotNull(email, username).size == 1
        if (!validArgs) bad("Only either 'email' or 'username' must be passed.")

        val user = userService.getSpecific(email = email, username = username)

        val authenticated = if (user != null) {
            encryptor.verify(password, user.password)
        } else false

        if (authenticated) return user!!
        else unauthorized()
    }

    override suspend fun loginUser(userId: Long): AuthTokens {
        val now = Clock.System.now()
        val expiringAt = now.plus(REFRESH_TOKEN_DURATION)

        val user = userService.getSpecific(id = userId) ?: notFoundIdentifier("user", userId.toString())

        val refreshToken = tokenService.create(
            type = TwoFactorTokenType.RefreshToken,
            userId = userId,
            expiringAt = expiringAt,
            issuedAt = now
        ) ?: notFoundIdentifier("user", userId.toString())

        val jwt = JWT.create()
            .withExpiresAt(expiringAt.toJavaInstant())
            .withIssuedAt(now.toJavaInstant())
            .withSubject(user.email)
            .sign(Algorithm.HMAC256(JWT_SIGNING_SECRET))

        return AuthTokens(jwt, refreshToken)
    }

    override suspend fun logoutUser(userId: Long, vararg tokens: UUID) {
        val user = userService.getSpecific(id = userId)
            ?: notFoundIdentifier("user", userId.toString())

        tokens.forEach { token ->
            val tokenModel = tokenService.getSpecific(token = token)
            if(tokenModel?.user?.id == user.id)
                tokenService.markAsUsed(tokenModel.id)
        }
    }
}