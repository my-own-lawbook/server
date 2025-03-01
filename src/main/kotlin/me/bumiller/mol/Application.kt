package me.bumiller.mol

import io.ktor.server.application.*
import io.ktor.server.netty.*
import me.bumiller.mol.core.di.dataServiceModule
import me.bumiller.mol.core.di.servicesModule
import me.bumiller.mol.database.di.databaseModule
import me.bumiller.mol.database.table.*
import me.bumiller.mol.database.table.crossref.LawBookMembersCrossref
import me.bumiller.mol.email.di.emailModule
import me.bumiller.mol.model.config.AppConfig
import me.bumiller.mol.rest.restApi
import me.bumiller.mol.validation.di.validationModule
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.restApi() {
    val appConfig = appConfig()

    setupDatabase(appConfig)
    setupKoin(appConfig)

    restApi(appConfig())
}

private fun setupDatabase(appConfig: AppConfig) {
    Database.connect(
        url = appConfig.databaseUrl,
        user = appConfig.databaseUser,
        password = appConfig.databasePassword
    )

    transaction {
        SchemaUtils.createMissingTablesAndColumns(
            User.Table,
            UserProfile.Table,
            TwoFactorToken.Table,
            LawBook.Table,
            BookInvitation.Table,
            LawEntry.Table,
            LawSection.Table,
            LawBookMembersCrossref.Table
        )
    }
}

private fun Application.setupKoin(appConfig: AppConfig) {
    install(Koin) {
        val appModule = module {
            single { appConfig }
        }

        modules(appModule)
        modules(databaseModule)
        modules(emailModule)
        modules(validationModule)
        modules(servicesModule, dataServiceModule)
    }
}

private fun Application.appConfig(): AppConfig = environment.config.run {
    println(keys().filter { it.startsWith("mol") }.toList())
    AppConfig(
        jwtSecret = property("civoris.security.jwt.secret").getString(),
        jwtDuration = property("civoris.security.jwt.duration").getString().toLong().minutes,
        refreshDuration = property("civoris.security.refresh.token.duration").getString().toLong().days,
        emailTokenDuration = property("civoris.security.email.token.duration").getString().toLong().minutes,
        databaseUrl = property("civoris.database.url").getString(),
        databaseUser = property("civoris.database.user").getString(),
        databasePassword = property("civoris.database.password").getString(),
        mailSmtpServer = property("civoris.mail.host").getString(),
        mailSmtpPort = property("civoris.mail.port").getString().toInt(),
        mailDoSsl = property("civoris.mail.ssl").getString().toBoolean(),
        mailUsername = property("civoris.mail.from").getString(),
        mailPassword = property("civoris.mail.password").getString(),
        basePath = property("civoris.rest.path").getString()
    )
}