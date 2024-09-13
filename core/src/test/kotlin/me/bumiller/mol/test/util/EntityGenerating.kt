package me.bumiller.mol.test.util

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import me.bumiller.mol.database.table.*

fun genderString(key: Long): String =
    if (key % 2 == 0L) "male"
    else if (key % 3 == 0L) "female"
    else if (key % 5 == 0L) "other"
    else "disclosed"

fun genderEntities(count: Int) = (1L..count).map(::genderString)

fun profileEntity(key: Long) = UserProfile.Model(
    key,
    LocalDate(2000, 1, 1).plus(DatePeriod(days = key.toInt())),
    "first_name-$key",
    "last_name-$key",
    genderString(key),
)

fun profileEntities(count: Long) = (1L..count).map(::profileEntity)

fun userEntity(key: Long) = User.Model(
    key,
    "email-$key",
    "username-$key",
    "password-$key",
    key % 2 == 0L,
    if (key % 3 == 0L) null else profileEntity(key)
)

fun userEntities(count: Long) = (1L..count).map(::userEntity)

fun lawBookEntity(key: Long) = LawBook.Model(
    key,
    "key-$key",
    "name-$key",
    "description-$key",
    userEntity(key),
    (key..key + 3).map(::userEntity)
)

fun lawBookEntities(count: Long) = (1L..count).map(::lawBookEntity)

fun lawEntryEntity(key: Long) = LawEntry.Model(
    key,
    "key-$key",
    "name-$key"
)

fun lawEntryEntities(count: Long) = (1L..count).map(::lawEntryEntity)

fun lawSectionEntity(key: Long) = LawSection.Model(
    key,
    "index-$key",
    "name-$key",
    "content-$key"
)

fun lawSectionEntities(count: Long) = (1L..count).map(::lawSectionEntity)