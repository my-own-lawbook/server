package me.bumiller.mol.rest.response.user

import kotlinx.serialization.Serializable
import me.bumiller.mol.model.User

/**
 * Response class that contains a user with its profile
 */
@Serializable
data class UserWithProfileResponse(

    val id: Long,

    val email: String,

    val username: String,

    val isEmailVerified: Boolean,

    val profile: UserProfileResponse

) {

    companion object {

        fun create(user: User): UserWithProfileResponse {
            require(user.profile != null) { "user.profile cannot be null" }
            return UserWithProfileResponse(
                id = user.id,
                email = user.email,
                username = user.username,
                isEmailVerified = user.isEmailVerified,
                profile = UserProfileResponse.create(user.profile!!)
            )
        }

    }

}