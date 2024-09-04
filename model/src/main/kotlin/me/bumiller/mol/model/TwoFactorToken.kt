package me.bumiller.mol.model

import kotlinx.datetime.Instant
import java.util.UUID

/**
 * Model for a token used for several security related processes
 */
data class TwoFactorToken (

    /**
     * The id
     */
    val id: Long,

    /**
     * The actual token
     */
    val token: UUID,

    /**
     * Optional additional info
     */
    val additionalInfo: String,

    /**
     * Time of ordering
     */
    val issuedAt: Instant,

    /**
     * Time of expiration
     */
    val expiringAt: Instant,

    /**
     * What context it was created in
     */
    val type: TwoFactorTokenType,

    /**
     * Whether it has already been used
     */
    val used: Boolean

)