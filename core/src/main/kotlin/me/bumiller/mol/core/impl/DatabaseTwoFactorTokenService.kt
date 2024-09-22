package me.bumiller.mol.core.impl

import kotlinx.datetime.Instant
import me.bumiller.mol.common.present
import me.bumiller.mol.common.presentWhenNotNull
import me.bumiller.mol.core.data.TwoFactorTokenService
import me.bumiller.mol.core.exception.ServiceException
import me.bumiller.mol.core.mapping.mapToken
import me.bumiller.mol.database.repository.TwoFactorTokenRepository
import me.bumiller.mol.database.repository.UserRepository
import me.bumiller.mol.model.TwoFactorToken
import me.bumiller.mol.model.TwoFactorTokenType
import java.util.*
import me.bumiller.mol.database.table.TwoFactorToken.Model as TwoFactorTokenModel

internal class DatabaseTwoFactorTokenService(
    val tokenRepository: TwoFactorTokenRepository,
    val userRepository: UserRepository
) : TwoFactorTokenService {

    override suspend fun getAll(): List<TwoFactorToken> = tokenRepository
        .getAll().map(::mapToken)

    override suspend fun getSpecific(id: Long?, token: UUID?) = tokenRepository
        .getSpecific(
            id = presentWhenNotNull(id),
            token = presentWhenNotNull(token)
        )?.let(::mapToken) ?: throw ServiceException.TwoFactorTokenNotFound(id, token)

    override suspend fun create(
        type: TwoFactorTokenType,
        userId: Long,
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
            token = UUID.randomUUID(),
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
}