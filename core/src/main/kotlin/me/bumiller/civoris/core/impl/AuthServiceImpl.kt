package me.bumiller.civoris.core.impl

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import me.bumiller.civoris.common.present
import me.bumiller.civoris.core.AuthService
import me.bumiller.civoris.core.EncryptionService
import me.bumiller.civoris.core.TokenGenerationStrategy
import me.bumiller.civoris.core.data.TwoFactorTokenService
import me.bumiller.civoris.core.data.UserService
import me.bumiller.civoris.core.exception.ServiceException
import me.bumiller.civoris.email.EmailService
import me.bumiller.civoris.model.AuthTokens
import me.bumiller.civoris.model.TwoFactorToken
import me.bumiller.civoris.model.TwoFactorTokenType
import me.bumiller.civoris.model.User
import me.bumiller.civoris.model.config.AppConfig

internal class AuthServiceImpl(
    val userService: UserService,
    val tokenService: TwoFactorTokenService,
    val encryptor: EncryptionService,
    val emailService: EmailService,
    val appConfig: AppConfig
) : AuthService {

    override suspend fun createNewUser(
        email: String,
        username: String,
        password: String,
        sendVerificationEmail: Boolean
    ): User {
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
            strategy = TokenGenerationStrategy.OneTimePassword(OTP_LENGTH),
            expiringAt = now.plus(appConfig.emailTokenDuration),
            issuedAt = now,
            additionalContent = user.email
        )

        emailService.sendEmailVerifyEmail(user, emailToken)

        return emailToken
    }

    override suspend fun getAuthenticatedUser(email: String?, username: String?, password: String): User? {
        val validArgs = listOfNotNull(email, username).size == 1
        require(validArgs) { "Both or neither of email and username were passed." }

        val user = try {
            userService.getSpecific(email = email, username = username, onlyActive = false)
        } catch (e: ServiceException.UserNotFound) {
            return null
        }

        return if (encryptor.verify(password, user.password)) return user
        else null
    }

    override suspend fun loginUser(userId: Long): AuthTokens {
        val now = Clock.System.now()
        val expiringAtRefresh = now.plus(appConfig.refreshDuration)
        val expiringAtJwt = now.plus(appConfig.jwtDuration)

        val user = userService.getSpecific(id = userId, onlyActive = false)

        val refreshToken = tokenService.create(
            type = TwoFactorTokenType.RefreshToken,
            userId = userId,
            strategy = TokenGenerationStrategy.UUID,
            expiringAt = expiringAtRefresh,
            issuedAt = now
        )

        val jwt = JWT.create()
            .withExpiresAt(expiringAtJwt.toJavaInstant())
            .withIssuedAt(now.toJavaInstant())
            .withSubject(user.email)
            .sign(Algorithm.HMAC256(appConfig.jwtSecret))

        return AuthTokens(jwt, refreshToken)
    }

    override suspend fun logoutUser(userId: Long, vararg tokens: String) {
        val user = userService.getSpecific(id = userId, onlyActive = false)

        tokens.forEach { token ->
            val tokenModel = tokenService.getSpecific(token = token)
            if (tokenModel.user.id == user.id && tokenModel.type == TwoFactorTokenType.RefreshToken)
                tokenService.markAsUsed(tokenModel.id)
        }
    }

    override suspend fun loginUserWithRefreshToken(token: String): AuthTokens {
        val tokenEntity = validateToken(token, TwoFactorTokenType.RefreshToken)

        tokenService.markAsUsed(tokenEntity.id)
        return loginUser(tokenEntity.user.id)
    }

    override suspend fun validateEmailWithToken(token: String): User {
        val tokenEntity = validateToken(token, TwoFactorTokenType.EmailConfirm)
        val user = tokenEntity.additionalInfo?.let { userService.getSpecific(email = it, onlyActive = false) }!!

        if (user.isEmailVerified)
            throw ServiceException.EmailTokenUserAlreadyVerified(token)

        tokenService.markAsUsed(tokenEntity.id)
        return userService.update(
            userId = user.id,
            isEmailVerified = present(true)
        )
    }

    private suspend fun validateToken(token: String, type: TwoFactorTokenType): TwoFactorToken {
        val now = Clock.System.now()

        val tokenEntity = tokenService.getSpecific(token = token)

        if (tokenEntity.type != type)
            throw ServiceException.InvalidTwoFactorTokenType(tokenEntity.token, type)
        if (tokenEntity.expiringAt == null || tokenEntity.expiringAt!! < now)
            throw ServiceException.TwoFactorTokenExpired(tokenEntity.token, tokenEntity.expiringAt)
        if (tokenEntity.used)
            throw ServiceException.TwoFactorTokenUsed(tokenEntity.token)

        return tokenEntity
    }

    companion object {

        private const val OTP_LENGTH = 6;

    }

}