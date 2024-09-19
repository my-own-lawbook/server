package me.bumiller.mol.core.impl

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import me.bumiller.mol.common.present
import me.bumiller.mol.core.AuthService
import me.bumiller.mol.core.EncryptionService
import me.bumiller.mol.core.data.TwoFactorTokenService
import me.bumiller.mol.core.data.UserService
import me.bumiller.mol.core.exception.ServiceException
import me.bumiller.mol.email.EmailService
import me.bumiller.mol.model.AuthTokens
import me.bumiller.mol.model.TwoFactorToken
import me.bumiller.mol.model.TwoFactorTokenType
import me.bumiller.mol.model.User
import me.bumiller.mol.model.config.AppConfig
import java.util.*

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
            userService.getSpecific(email = email, username = username)
        } catch (e: ServiceException.UserNotFound) {
            return null
        }

        val authenticated = encryptor.verify(password, user.password)

        return if (authenticated) return user
        else null
    }

    override suspend fun loginUser(userId: Long): AuthTokens {
        val now = Clock.System.now()
        val expiringAtRefresh = now.plus(appConfig.refreshDuration)
        val expiringAtJwt = now.plus(appConfig.jwtDuration)

        val user = userService.getSpecific(id = userId)

        val refreshToken = tokenService.create(
            type = TwoFactorTokenType.RefreshToken,
            userId = userId,
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

    override suspend fun logoutUser(userId: Long, vararg tokens: UUID) {
        val user = userService.getSpecific(id = userId)

        tokens.forEach { token ->
            val tokenModel = tokenService.getSpecific(token = token)
            if (tokenModel.user.id == user.id && tokenModel.type == TwoFactorTokenType.RefreshToken)
                tokenService.markAsUsed(tokenModel.id)
        }
    }

    override suspend fun loginUserWithRefreshToken(uuid: UUID): AuthTokens {
        val token = validateToken(uuid, TwoFactorTokenType.RefreshToken)

        tokenService.markAsUsed(token.id)
        return loginUser(token.user.id)
    }

    override suspend fun validateEmailWithToken(tokenUUID: UUID): User {
        val token = validateToken(tokenUUID, TwoFactorTokenType.EmailConfirm)
        val user = token.additionalInfo?.let { userService.getSpecific(email = it) }!!

        if (user.isEmailVerified)
            throw ServiceException.EmailTokenUserAlreadyVerified(tokenUUID)

        tokenService.markAsUsed(token.id)
        return userService.update(
            userId = user.id,
            isEmailVerified = present(true)
        )
    }

    private suspend fun validateToken(uuid: UUID, type: TwoFactorTokenType): TwoFactorToken {
        val now = Clock.System.now()

        val token = tokenService.getSpecific(token = uuid)

        if (token.type != type)
            throw ServiceException.InvalidTwoFactorTokenType(uuid, type)
        if (token.expiringAt == null || token.expiringAt!! < now)
            throw ServiceException.TwoFactorTokenExpired(uuid, token.expiringAt)
        if (token.used)
            throw ServiceException.TwoFactorTokenUsed(uuid)

        return token
    }
}