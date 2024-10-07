package me.bumiller.mol.core.impl

import kotlinx.datetime.LocalDate
import me.bumiller.mol.common.Optional
import me.bumiller.mol.common.present
import me.bumiller.mol.common.presentWhenNotNull
import me.bumiller.mol.core.data.UserService
import me.bumiller.mol.core.exception.ServiceException
import me.bumiller.mol.core.mapping.mapGenderString
import me.bumiller.mol.core.mapping.mapUser
import me.bumiller.mol.database.repository.UserProfileRepository
import me.bumiller.mol.database.repository.UserRepository
import me.bumiller.mol.model.Gender
import me.bumiller.mol.model.User
import me.bumiller.mol.model.UserProfile
import me.bumiller.mol.database.table.User.Model as UserModel
import me.bumiller.mol.database.table.UserProfile.Model as ProfileModel

internal class DatabaseUserService(
    val userRepository: UserRepository,
    val profileRepository: UserProfileRepository
) : UserService {

    override suspend fun getAll(): List<User> = userRepository.getAll()
        .map(::mapUser)

    override suspend fun getSpecific(id: Long?, email: String?, username: String?, onlyActive: Boolean) = userRepository
        .getSpecific(
            id = presentWhenNotNull(id),
            email = presentWhenNotNull(email),
            username = presentWhenNotNull(username),
            onlyActive = onlyActive
        )?.let(::mapUser) ?: throw ServiceException.UserNotFound(id = id, email = email, username = username)

    override suspend fun createUser(email: String, password: String, username: String): User {
        userRepository.getSpecific(email = present(email), onlyActive = false)?.let {
            throw ServiceException.UserEmailNotUnique(email)
        }
        userRepository.getSpecific(username = present(username), onlyActive = false)?.let {
            throw ServiceException.UserUsernameNotUnique(username = username)
        }

        val model = UserModel(
            id = -1L,
            email = email,
            username = username,
            password = password,
            isEmailVerified = false,
            profile = null
        )
        return userRepository.create(model, null)!!.let(::mapUser)
    }

    override suspend fun createProfile(userId: Long, profile: UserProfile): User {
        val user =
            userRepository.getSpecific(id = present(userId), onlyActive = false) ?: throw ServiceException.UserNotFound(
                id = userId
            )
        if (user.profile != null) throw ServiceException.UserProfileAlreadyPresent(userId = userId)

        val profileModel = ProfileModel(
            id = userId,
            birthday = profile.birthday,
            firstName = profile.firstName,
            lastName = profile.lastName,
            gender = mapGenderString(profile.gender)
        ).let { profileRepository.create(it) }

        val updated = user.copy(profile = profileModel)
        return userRepository.update(updated)!!
            .let(::mapUser)
    }

    override suspend fun deleteUser(userId: Long) =
        userRepository.delete(userId)
            ?.let(::mapUser) ?: throw ServiceException.UserNotFound(id = userId)

    override suspend fun update(
        userId: Long,
        email: Optional<String>,
        username: Optional<String>,
        password: Optional<String>,
        isEmailVerified: Optional<Boolean>
    ): User {
        val user =
            userRepository.getSpecific(id = present(userId), onlyActive = false) ?: throw ServiceException.UserNotFound(
                id = userId
            )

        email.ifPresentSuspend {
            userRepository.getSpecific(email = email)?.let {
                throw ServiceException.UserEmailNotUnique(email = email.get())
            }
        }
        username.ifPresentSuspend {
            userRepository.getSpecific(username = username)?.let {
                throw ServiceException.UserUsernameNotUnique(username = username.get())
            }
        }

        val updated = user.copy(
            email = email.getOr(user.email),
            username = username.getOr(user.username),
            password = password.getOr(user.password),
            isEmailVerified = isEmailVerified.getOr(user.isEmailVerified)
        )

        return userRepository.update(updated)!!
            .let(::mapUser)
    }

    override suspend fun updateProfile(
        userId: Long,
        firstName: Optional<String>,
        lastName: Optional<String>,
        birthday: Optional<LocalDate>,
        gender: Optional<Gender>
    ): User {
        val user = userRepository.getSpecific(id = userId) ?: throw ServiceException.UserNotFound(id = userId)
        val profile = user.profile ?: throw ServiceException.UserProfileNotPresent(userId = userId)

        val updated = profile.copy(
            firstName = firstName.getOr(profile.firstName),
            lastName = lastName.getOr(profile.lastName),
            birthday = birthday.getOr(profile.birthday),
            gender = gender.getOrNull()?.serializedName ?: profile.gender
        )

        profileRepository.update(updated)
        return userRepository.getSpecific(id = userId)!!
            .let(::mapUser)
    }
}