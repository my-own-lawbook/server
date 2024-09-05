package me.bumiller.mol.core.impl

import me.bumiller.mol.database.repository.UserRepository
import me.bumiller.mol.model.User
import me.bumiller.mol.model.UserProfile
import me.bumiller.mol.core.data.UserService
import me.bumiller.mol.core.mapping.mapGenderString
import me.bumiller.mol.core.mapping.mapUser
import java.util.*
import me.bumiller.mol.database.table.User.Model as UserModel
import me.bumiller.mol.database.table.UserProfile.Model as ProfileModel

internal class DatabaseUserService(
    val userRepository: UserRepository
) : UserService {

    override suspend fun getAll(): List<User> = userRepository.getAll()
        .map(::mapUser)

    override suspend fun getSpecific(id: Long?, email: String?, username: String?) = userRepository
        .getSpecific(
            id = Optional.ofNullable(id),
            email = Optional.ofNullable(email),
            username = Optional.ofNullable(username)
        )?.let(::mapUser)

    override suspend fun createUser(email: String, password: String, username: String): User {
        val model = UserModel(
            id = -1L,
            email = email,
            username = username,
            password = password,
            isEmailVerified = false,
            profile = null
        )
        return userRepository.create(model).let(::mapUser)
    }

    override suspend fun createProfile(userId: Long, profile: UserProfile): User? {
        val model = userRepository.getSpecific(userId) ?: return null
        val updated = model.copy(
            profile = ProfileModel(
                id = userId,
                birthday = profile.birthday,
                firstName = profile.firstName,
                lastName = profile.lastName,
                gender = mapGenderString(profile.gender)
            )
        )
        return userRepository.update(updated)?.let(::mapUser)
    }

    override suspend fun deleteUser(userId: Long) =
        userRepository.delete(userId)?.let(::mapUser)
}