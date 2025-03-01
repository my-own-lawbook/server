package me.bumiller.civoris.rest.response.user

import kotlinx.serialization.Serializable
import me.bumiller.civoris.model.User

/**
 * Class that contains information about a user together with its role in a law-book
 */
@Serializable
data class BookRoleUserResponse(

    val role: Int,

    val user: UserWithProfileResponse

) {

    companion object {

        fun create(role: Int, user: User) =
            BookRoleUserResponse(role, UserWithProfileResponse.create(user))

    }

}
