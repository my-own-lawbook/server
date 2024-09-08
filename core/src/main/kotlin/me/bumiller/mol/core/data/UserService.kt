package me.bumiller.mol.core.data

import me.bumiller.mol.model.User
import me.bumiller.mol.model.UserProfile
import java.util.*

/**
 * Interface to perform common operations on user
 */
interface UserService {

    /**
     * Gets all users
     *
     * @return A list of all users
     */
    suspend fun getAll(): List<User>

    /**
     * Gets a specific user identified by either one or several unique identifiers
     *
     * @param id The id  of the user
     * @param email The email of the user
     * @param username The username of the user
     * @return The user matching all given non-null criteria, or null
     */
    suspend fun getSpecific(id: Long? = null, email: String? = null, username: String? = null): User?

    /**
     * Creates a new user entry in the database.
     *
     * The created user will have no profile, i.e. [User.profile] = null and not have a verified email, i.e. [User.isEmailVerified] = false
     *
     * This will not trigger any auth actions but only create the user in the database.
     *
     * @param email The email
     * @param password The password, hashed
     * @param username The username
     */
    suspend fun createUser(email: String, password: String, username: String): User

    /**
     * Creates a profile in the database for the user with [userId].
     *
     * @param userId The id of the user
     * @param profile The profile to take content from. [UserProfile.id] is ignored.
     * @return The user for which the profile was created. Null if no user was found or a profile already exists.
     */
    suspend fun createProfile(userId: Long, profile: UserProfile): User?

    /**
     * Will delete a user in the database.
     *
     * @param userId The id of the user
     * @return The user that was deleted, or null if no user with [userId] was found.
     */
    suspend fun deleteUser(userId: Long): User?

    /**
     * Updates the specified (non-null) attributes of the user for [userId]
     *
     * @param userId The id of the user
     * @param email The new email
     * @param username The new username
     * @param password The new password
     * @param isEmailVerified The new password verified status
     * @return The updated user, or null if it was not found
     */
    suspend fun update(
        userId: Long,
        email: Optional<String> = Optional.empty(),
        username: Optional<String> = Optional.empty(),
        password: Optional<String> = Optional.empty(),
        isEmailVerified: Optional<Boolean> = Optional.empty()
    ): User?

}