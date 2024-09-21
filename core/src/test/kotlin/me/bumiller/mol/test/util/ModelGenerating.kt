package me.bumiller.mol.test.util

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import me.bumiller.mol.model.*

fun genderModel(key: Long): Gender =
    if (key % 2 == 0L) Gender.Male
    else if (key % 3 == 0L) Gender.Female
    else if (key % 5 == 0L) Gender.Other
    else Gender.Disclosed

fun genderModels(count: Int) = (1L..count).map(::genderModel)

fun profileModel(key: Long) = UserProfile(
    key,
    LocalDate(2000, 1, 1).plus(DatePeriod(days = key.toInt())),
    genderModel(key),
    "first_name-$key",
    "last_name-$key"
)

fun profileModels(count: Long) = (1L..count).map(::profileModel)

fun userModel(key: Long) = User(
    key,
    "email-$key",
    "username-$key",
    "password-$key",
    key % 2 == 0L,
    if (key % 3 == 0L) null else profileModel(key)
)

fun userModels(count: Long) = (1L..count).map(::userModel)

fun lawBookModel(key: Long) = LawBook(
    key,
    "key-$key",
    "name-$key",
    "description-$key",
    (key..key + 3).map(::userModel)
)

fun lawBookModels(count: Long) = (1L..count).map(::lawBookModel)

fun lawEntryModel(key: Long) = LawEntry(
    key,
    "key-$key",
    "name-$key"
)

fun lawEntryModels(count: Long) = (1L..count).map(::lawEntryModel)

fun lawSectionModel(key: Long) = LawSection(
    key,
    "index-$key",
    "name-$key",
    "content-$key"
)

fun lawSectionModels(count: Long) = (1L..count).map(::lawSectionModel)

fun memberRole(key: Long) =
    if (key % 2 == 0L) MemberRole.Admin
    else if (key % 3 == 0L) MemberRole.Write
    else if (key % 5 == 0L) MemberRole.Update
    else MemberRole.Read

fun invitationModel(key: Long) = BookInvitation(
    key,
    userModel(key),
    lawBookModel(key),
    userModel(key),
    memberRole(key),
    Instant.fromEpochSeconds(key * 1000),
    null,
    InvitationStatus.Open,
    null,
    null
)

fun invitationModels(count: Long) = (1L..count).map(::invitationModel)