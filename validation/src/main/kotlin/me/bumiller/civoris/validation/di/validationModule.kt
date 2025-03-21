package me.bumiller.civoris.validation.di

import me.bumiller.civoris.validation.AccessValidator
import me.bumiller.civoris.validation.impl.ServiceAccessValidator
import org.koin.dsl.module

/**
 * Module for the validation components
 */
val validationModule = module {
    single<AccessValidator> { ServiceAccessValidator(get(), get(), get(), get(), get()) }
}