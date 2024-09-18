package me.bumiller.mol.core.di

import me.bumiller.mol.core.AuthService
import me.bumiller.mol.core.EncryptionService
import me.bumiller.mol.core.LawService
import me.bumiller.mol.core.data.LawContentService
import me.bumiller.mol.core.data.MemberService
import me.bumiller.mol.core.data.TwoFactorTokenService
import me.bumiller.mol.core.data.UserService
import me.bumiller.mol.core.impl.*
import org.koin.dsl.module

/**
 * The module providing data services
 */
val dataServiceModule = module {
    single<UserService> { DatabaseUserService(get(), get()) }
    single<TwoFactorTokenService> { DatabaseTwoFactorTokenService(get(), get()) }
    single<MemberService> { DatabaseMemberService(get(), get(), get()) }
    single<LawContentService> { DatabaseLawContentService(get(), get(), get(), get()) }
}

/**
 * The module providing the services
 */
val servicesModule = module {
    single<EncryptionService> { BCryptEncryptionService() }
    single<AuthService> { AuthServiceImpl(get(), get(), get(), get(), get()) }
    single<LawService> { LawServiceImpl(get()) }
}