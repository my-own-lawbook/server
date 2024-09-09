package me.bumiller.mol.rest.response.user

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import me.bumiller.mol.model.Gender
import me.bumiller.mol.model.UserProfile

/**
 * Response class that contains information about a users profile
 */
@Serializable
data class UserProfileResponse(

    val birthday: LocalDate,

    val gender: Gender,

    val firstName: String,

    val lastName: String

)  {

    companion object {

        fun create(profile: UserProfile) = UserProfileResponse(
            birthday = profile.birthday,
            gender = profile.gender,
            firstName = profile.firstName,
            lastName = profile.lastName
        )

    }

}