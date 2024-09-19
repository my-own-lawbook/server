package me.bumiller.mol.model

/**
 * Role that is given to each member of a book.
 *
 * This defines how the member can access resources in the book.
 *
 * The permissions are propagated down, i.e. a user with role [Admin] has the stated privileges of [Admin] and all lower roles.
 */
sealed class MemberRole(val value: Int) {

    /**
     * Highes role.
     *
     * Permissions:
     * - Delete Book
     * - Edit book metadata, e.g. name, key description etc.
     * - Manage members, i.e. add new ones, remove existing ones and manage their role
     */
    data object Admin : MemberRole(4)

    /**
     * Second-highest role
     *
     * Permissions:
     * - Add and delete children resources of the book, e.g. entries or sections
     */
    data object Write : MemberRole(3)

    /**
     * Third-highest role
     *
     * Permission:
     * - Edit children resources of the book, e.g. entries or sections
     */
    data object Update : MemberRole(2)

    /**
     * Lowest role
     *
     * Permissions:
     * - Read data of the book and children resources, e.g. entries or sections
     * - Read the members and their respective roles of the book
     */
    data object Read : MemberRole(1)

    operator fun compareTo(other: MemberRole): Int =
        this.value.compareTo(other.value)

    /**
     * Utility function whether this role satisfies a required role
     *
     * @param requirement The requirement
     * @return Whether the requirement is met
     */
    infix fun satisfies(requirement: MemberRole): Boolean =
        this >= requirement

    companion object {

        /**
         * Contains all roles
         */
        val roles = listOf(Read, Update, Write, Admin)

    }

}