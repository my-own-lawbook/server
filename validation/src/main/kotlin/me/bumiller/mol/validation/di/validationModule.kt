package me.bumiller.mol.validation.di

import me.bumiller.mol.validation.AccessValidator
import me.bumiller.mol.validation.impl.ServiceAccessValidator
import org.koin.dsl.module

/**
 * Module for the validation components
 */
val validationModule = module {
    single<AccessValidator> { ServiceAccessValidator(get(), get(), get(), get()) }
}