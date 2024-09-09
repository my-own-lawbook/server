package me.bumiller.mol.rest.http.user

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.bumiller.mol.core.data.UserService
import me.bumiller.mol.model.Gender
import me.bumiller.mol.model.UserProfile
import me.bumiller.mol.model.http.conflict
import me.bumiller.mol.rest.plugins.authenticatedUser
import me.bumiller.mol.rest.response.user.UserWithProfileResponse
import me.bumiller.mol.rest.validation.*
import org.koin.ktor.ext.inject


internal fun Route.profile() {
    val userService by inject<UserService>()

    route("profile/") {
        createProfile(userService)
    }
}

//
// Request bodies
//

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
        firstName.validateProfileName()
        lastName.validateProfileName()
        birthday.validateOnlyPast()
    }

}

private fun Route.createProfile(userService: UserService) = post {
    val body = call.validated<CreateProfileRequest>()
    val user = call.authenticatedUser()

    if (user.profile != null) conflict("The authenticated user already has a profile set.")

    val profile = UserProfile(user.id, body.birthday, body.gender, body.firstName, body.lastName)

    val updatedUser = userService.createProfile(user.id, profile)!!

    call.respond(HttpStatusCode.OK, UserWithProfileResponse.create(updatedUser))
}