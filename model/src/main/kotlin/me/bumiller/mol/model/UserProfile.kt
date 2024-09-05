package me.bumiller.mol.model

import kotlinx.datetime.LocalDate

/**
 * Model for a users profile containing more, non-vital information
 */
data class UserProfile (

    /**
     * The id of the user
     */
    val id: Long,

    /**
     * The birthday
     */
    val birthday: LocalDate,

    /**
     * The gender
     */
    val gender: Gender,

    /**
     * The first name
     */
    val firstName: String,

    /**
     * The last name
     */
    val lastName: String

)