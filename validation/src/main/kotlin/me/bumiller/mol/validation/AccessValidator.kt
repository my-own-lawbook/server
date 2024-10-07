package me.bumiller.mol.validation

import me.bumiller.mol.model.http.RequestException

/**
 * Validator that helps to decide whether a user had access to a specific resource or not.
 */
interface AccessValidator {

    /**
     * Method to check whether a user has a specified [ScopedPermission] to perform an action.
     *
     * @param permission The [ScopedPermission] that will be checked for the user
     * @param userId The id of the user to check the permission on. If no user is found, 500 will be thrown
     * @param throwOnRestricted Whether to throw a [RequestException] if the validation fails, or to only return the result. Will throw 404 on non granted.
     * @return Whether the permission is granted
     */
    suspend fun resolveScoped(
        permission: ScopedPermission,
        userId: Long,
        throwOnRestricted: Boolean = true
    ): Boolean

    /**
     * Method to check whether a user has a specified [GlobalPermission] to perform an action.
     *
     * @param permission The [GlobalPermission] that will be checked for the user
     * @param userId The id of the user to check the permission on. If no user is found, 500 will be thrown
     * @param throwOnRestricted Whether to throw a [RequestException] if the validation fails, or to only return the result. Will throw 404 on non granted.
     * @return Whether the permission is granted
     */
    suspend fun resolveGlobal(
        permission: GlobalPermission,
        userId: Long,
        throwOnRestricted: Boolean = true
    ): Boolean

}