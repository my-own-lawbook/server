package me.bumiller.mol.validation

/**
 * Denotes the permission for a specific action (or scope of actions) that a user can be granted or not.
 */
sealed class GlobalPermission {

    /**
     * permissions related to users
     */
    sealed class Users : GlobalPermission() {

        /**
         * Permission to read all users
         */
        data class Read(val profile: Boolean = false, val email: Boolean = false) : Users()

    }

}