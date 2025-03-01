package me.bumiller.civoris.email.di

import me.bumiller.civoris.email.ApacheEmailService
import me.bumiller.civoris.email.EmailService
import org.koin.dsl.module

/**
 * Email module providing the [EmailService]
 */
val emailModule = module {
    single<EmailService> { ApacheEmailService(get()) }
}