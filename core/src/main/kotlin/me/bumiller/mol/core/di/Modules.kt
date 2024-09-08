package me.bumiller.mol.core.di

import me.bumiller.mol.core.AuthService
import me.bumiller.mol.core.EncryptionService
import me.bumiller.mol.core.data.TwoFactorTokenService
import me.bumiller.mol.core.data.UserService
import me.bumiller.mol.core.impl.AuthServiceImpl
import me.bumiller.mol.core.impl.BCryptEncryptionService
import me.bumiller.mol.core.impl.DatabaseTwoFactorTokenService
import me.bumiller.mol.core.impl.DatabaseUserService
import org.koin.dsl.module

/**
 * The module providing data services
 */
val dataServiceModule = module {
    single<UserService> { DatabaseUserService(get()) }
    single<TwoFactorTokenService> { DatabaseTwoFactorTokenService(get(), get()) }
}

/**
 * The module providing the services
 */
val servicesModule = module {
    single<EncryptionService> { BCryptEncryptionService() }
    single<AuthService> { AuthServiceImpl(get(), get(), get(), get(), get()) }
}