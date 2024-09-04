package me.bumiller.mol.model

/**
 * Model for a user with their profile information
 */
data class User(

    /**
     * The id
     */
    val id: Long,

    /**
     * The email
     */
    val email: String,

    /**
     * The username
     */
    val username: String,

    /**
     * The hashed password
     */
    val password: String,

    /**
     * The profile. Null if the user did not yet set up a profile
     */
    val profile: UserProfile?

)
