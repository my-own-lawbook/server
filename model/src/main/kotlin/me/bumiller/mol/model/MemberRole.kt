package me.bumiller.mol.model

/**
 * Role that is given to each member of a book.
 *
 * This defines how the member can access resources in the book.
 */
enum class MemberRole(val value: Int) {

    /**
     * Highes role.
     */
    Admin(3),

    /**
     * Second-highest role
     */
    Moderator(2),

    /**
     * Lowest role
     */
    Member(1)

}

/**
 * Utility function whether this role satisfies a required role
 *
 * @param requirement The requirement
 * @return Whether the requirement is met, or null if the member role is null
 */
infix fun MemberRole?.satisfies(requirement: MemberRole): Boolean =
    this?.let { this.value >= requirement.value } ?: false