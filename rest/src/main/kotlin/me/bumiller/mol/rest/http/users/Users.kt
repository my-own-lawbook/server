package me.bumiller.mol.rest.http.users

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.bumiller.mol.core.data.UserService
import me.bumiller.mol.rest.response.user.UserWithProfileResponse
import org.koin.ktor.ext.inject

/**
 * Adds the endpoints to /:
 *
 * - GET /users/: Gets all users in the server
 */
internal fun Route.users() {
    val userService by inject<UserService>()

    route("users/") {
        getUsers(userService)
    }
}

//
// Endpoints
//

/**
 * Endpoint to GET /users/ that gets all users
 */
private fun Route.getUsers(userService: UserService) = get {
    val users = userService.getAll()

    val response = users.map { UserWithProfileResponse.create(it) }
    call.respond(HttpStatusCode.OK, response)
}