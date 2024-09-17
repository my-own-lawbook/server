package me.bumiller.mol.rest.response.user

import kotlinx.serialization.Serializable
import me.bumiller.mol.model.User

/**
 * Response class that contains information about a user with its profile.
 *
 * Does not contain sensitive data, unlike [AuthUserWithProfileResponse] or [AuthUserWithoutProfileResponse].
 */
@Serializable
data class UserWithProfileResponse(

    val id: Long,

    val username: String,

    val profile: UserProfileResponse

) {

    companion object {

        fun create(user: User) =
            UserWithProfileResponse(user.id, user.username, UserProfileResponse.create(user.profile!!))

    }

}
