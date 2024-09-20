package me.bumiller.mol.validation

import me.bumiller.mol.model.http.RequestException

/**
 * Collections of scopes (law-resource types) that a user could request access for.
 */
enum class LawResourceScope {

    /**
     * Book-wide access
     */
    Book,

    /**
     * Entry-wide access
     */
    Entry,

    /**
     * Section-wide access
     */
    Section

}

/**
 * Collection of permissions a user could request for a specific [LawResourceScope].
 */
enum class LawPermission {

    /**
     * Can create children in the given [LawResourceScope]
     */
    Create,

    /**
     * Can edit the given [LawResourceScope]
     */
    Edit,

    /**
     * Can read the given [LawResourceScope]
     */
    Read

}

/**
 * Validator that helps to decide whether a user had access to a specific resource or not.
 */
interface AccessValidator {

    /**
     * Method to check whether a user has the rights to a permission on a specific law-scope.
     *
     * @param scope The [LawResourceScope]
     * @param permission The [LawPermission] on the scope
     * @param resourceId The id of the specific [LawResourceScope] resource.
     * @param userId The id of the user to check the permission on. If no user is found, 500 will be thrown
     * @param throwOnRestricted Whether to throw a [RequestException] if the validation fails, or to only return the result. Following scenarios are possible:
     * - User requested [LawPermission.Edit] or [LawPermission.Create] on a resource the user is allowed to view, but no more. Then, 401 is thrown, or false returned.
     * - User requested [LawPermission.Edit] or [LawPermission.Create] on a resource the user not allowed to view. Then, 404 is thrown, or false returned.
     * - User requested any [LawPermission] on a resource that could not be found. Then, 404 is thrown, or null returned.
     */
    suspend fun hasAccess(
        scope: LawResourceScope,
        permission: LawPermission,
        resourceId: Long,
        userId: Long,
        throwOnRestricted: Boolean = true
    ): Boolean?

}