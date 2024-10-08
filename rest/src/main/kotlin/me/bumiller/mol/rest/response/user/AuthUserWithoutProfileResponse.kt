package me.bumiller.mol.rest.response.user

import kotlinx.serialization.Serializable
import me.bumiller.mol.model.User

/**
 * Response for endpoints that return data about a user without the users profile
 */
@Serializable
internal data class AuthUserWithoutProfileResponse(

    val id: Long,

    val email: String,

    val username: String,

    val isEmailVerified: Boolean

) {

    companion object {

        fun create(user: User) =
            AuthUserWithoutProfileResponse(user.id, user.email, user.username, user.isEmailVerified)

    }

}
