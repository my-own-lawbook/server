package me.bumiller.mol.rest.http.user

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.bumiller.mol.common.Optional
import me.bumiller.mol.common.empty
import me.bumiller.mol.core.data.UserService
import me.bumiller.mol.model.Gender
import me.bumiller.mol.model.UserProfile
import me.bumiller.mol.model.http.conflict
import me.bumiller.mol.model.http.internal
import me.bumiller.mol.model.http.notFound
import me.bumiller.mol.rest.plugins.authenticatedUser
import me.bumiller.mol.rest.response.user.UserProfileResponse
import me.bumiller.mol.rest.response.user.UserWithProfileResponse
import me.bumiller.mol.rest.util.user
import me.bumiller.mol.rest.validation.*
import org.koin.ktor.ext.inject

/**
 * Adds the endpoints to /user/profile/:
 *
 * - POST /user/profile/: Create the profile
 * - PATCH /user/profile/: Update the profile
 * - GET /user/profile/: Get the profile
 */
internal fun Route.profile() {
    val userService by inject<UserService>()

    route("profile/") {
        createProfile(userService)
        getProfile()
        updateProfile(userService)
    }
}

//
// Request bodies
//

/**
 * Request body for a request to POST /user/profile/
 */
@Serializable
private data class CreateProfileRequest(

    @SerialName("first_name")
    val firstName: String,

    @SerialName("last_name")
    val lastName: String,

    val gender: Gender,

    val birthday: LocalDate

) : Validatable {

    override suspend fun ValidationScope.validate() {
        validateThat(firstName).isProfileName()
        validateThat(lastName).isProfileName()
        validateThat(birthday).isInPast()
    }

}

/**
 * Request body for a request to PATCH /user/profile/
 */
@Serializable
private data class UpdateProfileRequest(

    @SerialName("first_name")
    val firstName: Optional<String> = empty(),

    @SerialName("last_name")
    val lastName: Optional<String> = empty(),

    val birthday: Optional<LocalDate> = empty(),

    val gender: Optional<Gender> = empty()

) : Validatable {

    override suspend fun ValidationScope.validate() {
        validateThatOptional(firstName)?.isProfileName()
        validateThatOptional(lastName)?.isProfileName()
        validateThatOptional(birthday)?.isInPast()
    }

}

//
// Endpoint mappings
//

/**
 * Endpoint to /user/profile/ that lets the authenticated user set their profile
 */
private fun Route.createProfile(userService: UserService) = post {
    val body = call.validated<CreateProfileRequest>()

    if (user.profile != null) conflict("The authenticated user already has a profile set.")

    val profile = UserProfile(user.id, body.birthday, body.gender, body.firstName, body.lastName)

    val updatedUser = userService.createProfile(user.id, profile) ?: internal()

    call.respond(HttpStatusCode.OK, UserWithProfileResponse.create(updatedUser))
}

/**
 * Endpoint to /user/profile/ that lets the authenticated user update their profile
 */
private fun Route.getProfile() = get {
    if (user.profile == null) notFound("The authenticated user does not have a profile set.")
    else call.respond(HttpStatusCode.OK, UserProfileResponse.create(user.profile!!))
}

/**
 * Endpoint to /user/profile/ that lets the authenticated user update their profile
 */
private fun Route.updateProfile(userService: UserService) = patch {
    val body = call.validated<UpdateProfileRequest>()
    val user = call.authenticatedUser()

    val updatedUser = userService.updateProfile(
        userId = user.id,
        firstName = body.firstName,
        lastName = body.lastName,
        birthday = body.birthday,
        gender = body.gender
    )

    if (updatedUser != null)
        call.respond(HttpStatusCode.OK, UserWithProfileResponse.create(updatedUser))
    else internal()
}