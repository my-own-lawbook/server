package me.bumiller.mol.rest.http.auth

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.bumiller.mol.core.AuthService
import me.bumiller.mol.core.data.TwoFactorTokenService
import me.bumiller.mol.model.TwoFactorToken
import me.bumiller.mol.model.TwoFactorTokenType
import me.bumiller.mol.rest.http.PathToken
import me.bumiller.mol.rest.util.user
import me.bumiller.mol.rest.util.uuidOrBadRequest
import org.koin.ktor.ext.inject

/**
 * Sets up the routes:
 *
 * - POST /auth/logout/?token=<token>
 * - POST /auth/logout/all/
 */
internal fun Route.logout() {
    val authService by inject<AuthService>()
    val tokenService by inject<TwoFactorTokenService>()

    route("auth/logout/") {
        logout(authService)

        route("all/") {
            logoutAll(authService, tokenService)
        }
    }
}

//
// Endpoint mappings
//

/**
 * Route to POST auth/logout/?token=<token> to invalidate it
 */
fun Route.logout(authService: AuthService) = post {
    val toLogoutToken = call.request.queryParameters.uuidOrBadRequest(PathToken)

    try {
        authService.logoutUser(user.id, toLogoutToken)
    } finally {
        call.respond(HttpStatusCode.NoContent)
    }
}

/**
 * Route to POST auth/logout/all/ to invalidate all refresh tokens
 */
fun Route.logoutAll(authService: AuthService, tokenService: TwoFactorTokenService) = post {
    val toLogoutTokens = tokenService.getAll()
        .filter { token ->
            token.user.id == user.id &&
                    !token.used &&
                    token.type == TwoFactorTokenType.RefreshToken
        }.map(TwoFactorToken::token)

    try {
        authService.logoutUser(user.id, *toLogoutTokens.toTypedArray())
    } finally {
        call.respond(HttpStatusCode.NoContent)
    }
}