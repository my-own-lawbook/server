package me.bumiller.civoris.core.mapping

import me.bumiller.civoris.model.TwoFactorToken
import me.bumiller.civoris.model.TwoFactorTokenType
import me.bumiller.civoris.database.table.TwoFactorToken.Model as TwoFactorTokenModel

internal fun mapToken(token: TwoFactorTokenModel): TwoFactorToken =
    TwoFactorToken(
        id = token.id,
        token = token.token,
        additionalInfo = token.additionalContent,
        issuedAt = token.issuedAt,
        expiringAt = token.expiringAt,
        type = mapTokenType(token.type),
        used = token.used,
        user = mapUser(token.user)
    )

private fun mapTokenType(type: String): TwoFactorTokenType =
    TwoFactorTokenType.entries.find { it.serializedName == type }
        ?: throw IllegalStateException("Could not find a token type for string '$type'")