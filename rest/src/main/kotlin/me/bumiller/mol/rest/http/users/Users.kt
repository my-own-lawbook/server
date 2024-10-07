package me.bumiller.mol.rest.http.users

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.bumiller.mol.core.data.UserService
import me.bumiller.mol.rest.http.PathUserId
import me.bumiller.mol.rest.response.user.UserWithProfileResponse
import me.bumiller.mol.rest.util.longOrBadRequest
import me.bumiller.mol.rest.util.user
import me.bumiller.mol.validation.AccessValidator
import me.bumiller.mol.validation.GlobalPermission
import org.koin.ktor.ext.inject

/**
 * Adds the endpoints to /:
 *
 * - GET /users/: Gets all users in the server
 * - GET /users/:id/: Gets a specific user
 */
internal fun Route.users() {
    val userService by inject<UserService>()
    val accessValidator by inject<AccessValidator>()

    route("users/") {
        getUsers(userService, accessValidator)

        route("{$PathUserId}/") {
            getSpecificUser(userService, accessValidator)
        }
    }
}

//
// Endpoints
//

/**
 * Endpoint to GET /users/ that gets all users
 */
private fun Route.getUsers(userService: UserService, accessValidator: AccessValidator) = get {
    accessValidator.resolveGlobal(GlobalPermission.Users.Read(profile = true), user.id)

    val users = userService.getAll()

    val response = users.map { UserWithProfileResponse.create(it) }
    call.respond(HttpStatusCode.OK, response)
}

private fun Route.getSpecificUser(userService: UserService, accessValidator: AccessValidator) = get {
    accessValidator.resolveGlobal(GlobalPermission.Users.Read(profile = true), user.id)

    val otherUserId = call.parameters.longOrBadRequest(PathUserId)

    val otherUser = userService.getSpecific(id = otherUserId)

    val response = UserWithProfileResponse.create(otherUser)
    call.respond(HttpStatusCode.OK, response)
}