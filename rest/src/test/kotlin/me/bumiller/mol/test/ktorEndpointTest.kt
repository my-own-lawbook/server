package me.bumiller.mol.test

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import me.bumiller.mol.core.*
import me.bumiller.mol.core.data.*
import me.bumiller.mol.model.User
import me.bumiller.mol.model.config.AppConfig
import me.bumiller.mol.rest.restApi
import me.bumiller.mol.validation.AccessValidator
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

// App config not used in rest
private val appConfig: AppConfig = mockk()

/**
 * Object that contains all the services used by the REST API.
 *
 * Bundles together for a concise mocking-dsl in the [ktorEndpointTest].
 */
data class Services(

    val userService: UserService = mockk(),

    val tokenService: TwoFactorTokenService = mockk(),

    val authService: AuthService = mockk(),

    val encryptionService: EncryptionService = mockk(),

    val lawService: LawService = mockk(),

    val lawContentService: LawContentService = mockk(),

    val memberContentService: MemberContentService = mockk(),

    val memberService: MemberService = mockk(),

    val accessValidator: AccessValidator = mockk(),

    val invitationService: InvitationService = mockk(),

    val invitationContentService: InvitationContentService = mockk()

)

/**
 * Wrapper for a test that will execute requests to endpoints of the REST API.
 *
 * @param authenticatedUser The user to create authentication headers and service mockking for
 * @param testContent The actual location for tests.
 */
fun ktorEndpointTest(
    authenticatedUser: User? = null,
    testContent: suspend ApplicationTestBuilder.(Services, HttpClient) -> Unit
) = testApplication {
    every { appConfig.jwtSecret } returns "289499da-4592-4e7e-8f0b-a303d4c45ec8"

    val services = Services()

    if (authenticatedUser != null) {
        coEvery {
            services.userService.getSpecific(
                any(),
                eq(authenticatedUser.email),
                any(),
                any()
            )
        } returns authenticatedUser
    }

    /*
     * AccessService is mocked to always return true for access. Can be overwritten
     */
    coEvery { services.accessValidator.resolveScoped(any(), any(), any()) } returns true
    coEvery { services.accessValidator.resolveGlobal(any(), any(), any()) } returns true

    application {
        install(Koin) {
            services.run {
                modules(module {
                    single { userService }
                    single { tokenService }
                    single { authService }
                    single { encryptionService }
                    single { lawService }
                    single { lawContentService }
                    single { memberService }
                    single { memberContentService }
                    single { accessValidator }
                    single { invitationService }
                    single { invitationContentService }
                })
            }
        }

        restApi(appConfig, "test/api/")
    }

    val client = createClient {
        install(ContentNegotiation) {
            json()
        }

        if (authenticatedUser != null) {
            authHeaderFor(authenticatedUser, appConfig.jwtSecret)
        }
    }

    testContent(services, client)
}

private fun HttpClientConfig<out HttpClientEngineConfig>.authHeaderFor(user: User, jwtSecret: String) {
    val jwt = JWT.create()
        .withSubject(user.email)
        .sign(Algorithm.HMAC256(jwtSecret))

    defaultRequest {
        header("Authorization", "Bearer $jwt")
    }
}