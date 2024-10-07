package me.bumiller.mol.email.di

import me.bumiller.mol.email.ApacheEmailService
import me.bumiller.mol.email.EmailService
import org.koin.dsl.module

/**
 * Email module providing the [EmailService]
 */
val emailModule = module {
    single<EmailService> { ApacheEmailService(get()) }
}