package me.bumiller.civoris.core.impl

import kotlinx.datetime.Instant
import me.bumiller.civoris.common.present
import me.bumiller.civoris.common.presentWhenNotNull
import me.bumiller.civoris.core.TokenGenerationStrategy
import me.bumiller.civoris.core.data.TwoFactorTokenService
import me.bumiller.civoris.core.exception.ServiceException
import me.bumiller.civoris.core.mapping.mapToken
import me.bumiller.civoris.database.repository.TwoFactorTokenRepository
import me.bumiller.civoris.database.repository.UserRepository
import me.bumiller.civoris.model.TwoFactorToken
import me.bumiller.civoris.model.TwoFactorTokenType
import java.lang.Math.pow
import java.security.SecureRandom
import java.util.*
import me.bumiller.civoris.database.table.TwoFactorToken.Model as TwoFactorTokenModel

internal class DatabaseTwoFactorTokenService(
    val tokenRepository: TwoFactorTokenRepository,
    val userRepository: UserRepository
) : TwoFactorTokenService {

    private val random = SecureRandom()

    override suspend fun getAll(): List<TwoFactorToken> = tokenRepository
        .getAll().map(::mapToken)

    override suspend fun getSpecific(id: Long?, token: String?) = tokenRepository
        .getSpecific(
            id = presentWhenNotNull(id),
            token = presentWhenNotNull(token)
        )?.let(::mapToken) ?: throw ServiceException.TwoFactorTokenNotFound(id, token)

    override suspend fun create(
        type: TwoFactorTokenType,
        userId: Long,
        strategy: TokenGenerationStrategy,
        expiringAt: Instant?,
        issuedAt: Instant,
        additionalContent: String?
    ): TwoFactorToken {
        val user =
            userRepository.getSpecific(id = present(userId), onlyActive = false) ?: throw ServiceException.UserNotFound(
                id = userId
            )
        val model = TwoFactorTokenModel(
            id = -1,
            token = generateToken(strategy),
            issuedAt = issuedAt,
            expiringAt = expiringAt,
            used = false,
            additionalContent = additionalContent,
            type = type.serializedName,
            user = user
        )

        return tokenRepository.create(model, user.id)!!.let(::mapToken)
    }

    override suspend fun markAsUsed(tokenId: Long): TwoFactorToken {
        val token = tokenRepository.getSpecific(tokenId) ?: throw ServiceException.TwoFactorTokenNotFound(id = tokenId)
        val updated = token.copy(used = true)
        return tokenRepository.update(updated)!!.let(::mapToken)
    }

    private suspend fun generateToken(strategy: TokenGenerationStrategy) = when (strategy) {
        is TokenGenerationStrategy.OneTimePassword -> {
            tokenRepository.deleteExpired()

            val lowerBound = pow(10.0, strategy.length.toDouble()).toInt()
            val upperBound = pow(10.0, strategy.length.toDouble() + 1.0).toInt()
            val boundDif = upperBound - lowerBound

            var token = random.nextInt(boundDif) + lowerBound

            while (tokenRepository.getSpecific(token = present(token.toString())) != null) {
                token = random.nextInt(boundDif) + lowerBound
            }

            token.toString()
        }

        TokenGenerationStrategy.UUID -> UUID.randomUUID().toString()
    }
}