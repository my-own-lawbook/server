package me.bumiller.mol.database.di

import me.bumiller.mol.database.repository.*
import org.koin.dsl.module

/**
 * Database module providing the repositories
 */
val databaseModule = module {
    single<UserRepository> { ExposedUserRepository() }
    single<TwoFactorTokenRepository> { ExposedTwoFactorTokenRepository() }
    single<UserProfileRepository> { ExposedUserProfileRepository() }
}