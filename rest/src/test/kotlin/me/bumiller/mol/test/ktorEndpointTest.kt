package me.bumiller.mol.test

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.testing.*
import io.mockk.every
import io.mockk.mockk
import me.bumiller.mol.common.lazyWithReceiver
import me.bumiller.mol.core.AuthService
import me.bumiller.mol.core.EncryptionService
import me.bumiller.mol.core.LawService
import me.bumiller.mol.core.data.LawContentService
import me.bumiller.mol.core.data.TwoFactorTokenService
import me.bumiller.mol.core.data.UserService
import me.bumiller.mol.model.config.AppConfig
import me.bumiller.mol.rest.restApi
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

    val lawContentService: LawContentService = mockk()

)

/**
 * Wrapper for a test that will execute requests to endpoints of the REST API.
 *
 * @param testContent The actual location for tests.
 */
fun ktorEndpointTest(
    testContent: suspend ApplicationTestBuilder.(Services) -> Unit
) = testApplication {
    every { appConfig.jwtSecret } returns "289499da-4592-4e7e-8f0b-a303d4c45ec8"
    val services = Services()

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
                })
            }
        }

        restApi(appConfig, "test/api")
    }

    testContent(services)
}

/**
 * Helper function to reference an [HttpClient] that is preconfigured with the necessary config.
 *
 * Sadly, we cannot override [ApplicationTestBuilder.client], so we need to name this property "testClient".
 */
val ApplicationTestBuilder.testClient: HttpClient by lazyWithReceiver {
    createClient {
        install(ContentNegotiation) {
            json()
        }
    }
}