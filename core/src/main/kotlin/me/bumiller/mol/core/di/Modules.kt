package me.bumiller.mol.core.di

import me.bumiller.mol.core.*
import me.bumiller.mol.core.data.*
import me.bumiller.mol.core.impl.*
import org.koin.dsl.module

/**
 * The module providing data services
 */
val dataServiceModule = module {
    single<UserService> { DatabaseUserService(get(), get()) }
    single<TwoFactorTokenService> { DatabaseTwoFactorTokenService(get(), get()) }
    single<MemberContentService> { DatabaseMemberContentService(get(), get(), get()) }
    single<LawContentService> { DatabaseLawContentService(get(), get(), get(), get(), get()) }
    single<InvitationContentService> { DatabaseInvitationContentService(get(), get(), get()) }
}

/**
 * The module providing the services
 */
val servicesModule = module {
    single<EncryptionService> { BCryptEncryptionService() }
    single<AuthService> { AuthServiceImpl(get(), get(), get(), get(), get()) }
    single<LawService> { LawServiceImpl(get()) }
    single<MemberService> { MemberServiceImpl(get(), get()) }
    single<InvitationService> { InvitationServiceImpl(get(), get()) }
}