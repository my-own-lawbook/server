package me.bumiller.mol.core.mapping

import me.bumiller.mol.model.Gender
import me.bumiller.mol.model.User
import me.bumiller.mol.model.UserProfile
import me.bumiller.mol.database.table.User.Model as UserModel

internal fun mapUser(user: UserModel): User =
    User(
        id = user.id,
        email = user.email,
        username = user.username,
        password = user.password,
        isEmailVerified = user.isEmailVerified,
        profile = user.profile?.let { profile ->
            UserProfile(
                id = profile.id,
                birthday = profile.birthday,
                gender = mapGender(profile.gender),
                firstName = profile.firstName,
                lastName = profile.lastName
            )
        }
    )

internal fun mapGender(gender: String): Gender =
    when (gender) {
        "male" -> Gender.Male
        "female" -> Gender.Female
        "other" -> Gender.Other
        "disclosed" -> Gender.Disclosed
        else -> error("No gender found for string '$gender'")
    }

internal fun mapGenderString(gender: Gender): String = when (gender) {
    Gender.Disclosed -> "disclosed"
    Gender.Female -> "female"
    Gender.Male -> "male"
    Gender.Other -> "other"
}