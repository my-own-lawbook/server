package me.bumiller.mol.rest.http.user

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import me.bumiller.mol.common.Optional
import me.bumiller.mol.common.empty
import me.bumiller.mol.core.data.UserService
import me.bumiller.mol.core.exception.ServiceException
import me.bumiller.mol.model.Gender
import me.bumiller.mol.model.UserProfile
import me.bumiller.mol.model.http.internal
import me.bumiller.mol.rest.plugins.authenticatedUser
import me.bumiller.mol.rest.response.user.AuthUserWithProfileResponse
import me.bumiller.mol.rest.response.user.AuthUserWithoutProfileResponse
import me.bumiller.mol.rest.response.user.UserProfileResponse
import me.bumiller.mol.rest.util.user
import me.bumiller.mol.validation.Validatable
import me.bumiller.mol.validation.actions.isInPast
import me.bumiller.mol.validation.actions.isProfileName
import me.bumiller.mol.validation.validateThat
import me.bumiller.mol.validation.validateThatOptional
import me.bumiller.mol.validation.validated
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

    route("user/") {
        route("profile/") {
            createProfile(userService)
            getProfile()
            updateProfile(userService)
        }

        getUserInfo()
    }
}

//
// Request bodies
//

/**
 * Request body for a request to POST /user/profile/
 */
@Serializable
internal data class CreateProfileRequest(

    val firstName: String,

    val lastName: String,

    val gender: Gender,

    val birthday: LocalDate

) : Validatable {

    override suspend fun validate() {
        validateThat(firstName).isProfileName()
        validateThat(lastName).isProfileName()
        validateThat(birthday).isInPast()
    }

}

/**
 * Request body for a request to PATCH /user/profile/
 */
@Serializable
internal data class UpdateProfileRequest(

    val firstName: Optional<String> = empty(),

    val lastName: Optional<String> = empty(),

    val birthday: Optional<LocalDate> = empty(),

    val gender: Optional<Gender> = empty()

) : Validatable {

    override suspend fun validate() {
        validateThatOptional(firstName)?.isProfileName()
        validateThatOptional(lastName)?.isProfileName()
        validateThatOptional(birthday)?.isInPast()
    }

}

//
// Endpoint mappings
//

/**
 * Endpoint to /user/ that returns the user data
 */
private fun Route.getUserInfo() = get {
    call.respond(HttpStatusCode.OK, AuthUserWithoutProfileResponse.create(user))
}

/**
 * Endpoint to /user/profile/ that lets the authenticated user set their profile
 */
private fun Route.createProfile(userService: UserService) = post {
    val body = call.validated<CreateProfileRequest>()

    val profile = UserProfile(user.id, body.birthday, body.gender, body.firstName, body.lastName)

    val updatedUser = try {
        userService.createProfile(user.id, profile)
    } catch (e: ServiceException.UserNotFound) {
        internal()
    }

    val response = AuthUserWithProfileResponse.create(updatedUser)
    call.respond(HttpStatusCode.OK, response)
}

/**
 * Endpoint to /user/profile/ that lets the authenticated user update their profile
 */
private fun Route.getProfile() = get {
    call.respond(HttpStatusCode.OK, UserProfileResponse.create(user.profile!!))
}

/**
 * Endpoint to /user/profile/ that lets the authenticated user update their profile
 */
@SuppressWarnings("kotlin:S1481")
private fun Route.updateProfile(userService: UserService) = patch {
    val body = call.validated<UpdateProfileRequest>()
    val user = call.authenticatedUser()

    val updatedUser = try {
        userService.updateProfile(
            userId = user.id,
            firstName = body.firstName,
            lastName = body.lastName,
            birthday = body.birthday,
            gender = body.gender
        )
    } catch (e: ServiceException.UserNotFound) {
        internal()
    }

    val response = AuthUserWithProfileResponse.create(updatedUser)
    call.respond(HttpStatusCode.OK, response)
}