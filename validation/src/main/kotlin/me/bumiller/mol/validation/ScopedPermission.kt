package me.bumiller.mol.validation

/**
 * Denotes the permission for a specific action (or scope of actions) that a user can be granted or not.
 *
 * Is scoped to a specific resource, like for example "the book with id 4".
 */
sealed class ScopedPermission(

    /**
     * The name of the resource
     */
    val resourceName: String,

    /**
     * The id of the specific resource the permission may be bound to
     */
    open val id: Long

) {

    /**
     * Permissions related to users
     */
    sealed class Users(id: Long) : ScopedPermission("user", id) {

        /**
         * Permission to read a specific user.
         *
         * @param id The id of the user.
         * @param profile Whether access to the users profile is included
         * @param email Whether access to the users email is included
         */
        class Read(val profile: Boolean = false, val email: Boolean = false, id: Long) : Users(id)

    }

    /**
     * Permissions related to law-sections
     */
    sealed class Sections(id: Long) : ScopedPermission("law-section", id) {

        /**
         * Permission to read a specific section.
         *
         * @param id The id of the section.
         */
        data class Read(override val id: Long) : Sections(id)

        /**
         * Permission to write to a specific section, i.e. change attributes and delete it.
         *
         * @param id The id of the section.
         */
        data class Write(override val id: Long) : Sections(id)

    }

    /**
     * Permissions related to law-entries
     */
    sealed class Entries(id: Long) : ScopedPermission("law-entry", id) {

        /**
         * Permission to read a specific entry.
         *
         * @param id The id of the entry.
         */
        data class Read(override val id: Long) : Entries(id)

        /**
         * Permission to write to a specific entry, i.e. change attributes and delete it.
         *
         * @param id The id of the entry.
         */
        data class Write(override val id: Long) : Entries(id)

        /**
         * Permissions related to children of law-entries
         */
        sealed class Children(id: Long) : Entries(id) {

            /**
             * Permission to read all children of the entry
             *
             * @param id The id of the entry
             */
            data class Read(override val id: Long) : Children(id)
            
            /**
             * Permission to create children in the entry
             *
             * @param id The id of the entry
             */
            data class Create(override val id: Long) : Children(id)

        }

    }

    /**
     * Permissions related to law-books
     */
    sealed class Books(id: Long) : ScopedPermission("law-book", id) {

        /**
         * Permission to read a specific book.
         *
         * @param id The id of the book.
         */
        data class Read(override val id: Long) : Books(id)

        /**
         * Permission to write to a specific book, i.e. change attributes and delete it.
         *
         * @param id The id of the book.
         */
        data class Write(override val id: Long) : Books(id)

        /**
         * Permissions related to all children of a book
         */
        sealed class Children(id: Long) : Books(id) {

            /**
             * Permission to read all children of the book
             *
             * @param id The id of the book
             */
            data class Read(override val id: Long) : Children(id)

            /**
             * Permission to create children in the book
             *
             * @param id The id of the book
             */
            data class Create(override val id: Long) : Children(id)

        }

        /**
         * Permissions related to the members of a book
         */
        sealed class Members(id: Long) : Books(id) {

            /**
             * Permission to read the members of the book
             *
             * @param id The id of the book
             */
            data class Read(override val id: Long) : Members(id)

            /**
             * Permission to remove a member from a book
             *
             * @param id The id of the book
             */
            data class Remove(override val id: Long) : Members(id)

            /**
             * Permission to view the invitations of the book
             *
             * @param id The id of the book
             */
            data class ReadInvitations(override val id: Long) : Members(id)

            /**
             * Permission to manage the invitations of the book, i.e. create new ones and revoke other ones
             *
             * @param id The id of the book
             */
            data class ManageInvitations(override val id: Long) : Members(id)

        }

        /**
         * Permissions related to the member-roles inside a book
         */
        sealed class Roles(id: Long): Books(id) {

            /**
             * Permission to read the roles of members
             *
             * @param id The id of the book
             */
            data class Read(override val id: Long) : Roles(id)

            /**
             * Permission to set the roles of members
             *
             * @param id The id of the book
             */
            data class Write(override val id: Long) : Roles(id)

        }

    }

    /**
     * Permissions related to book-invitations
     */
    sealed class Invitations(id: Long) : ScopedPermission("book-invitation", id) {

        /**
         * Permission to read a specific invitation
         *
         * @param id The id of the invitation
         */
        data class Read(override val id: Long) : Invitations(id)

        /**
         * Permission to accept a specific invitation
         *
         * @param id The id of the invitation
         */
        data class Accept(override val id: Long) : Invitations(id)

        /**
         * Permission to deny a specific invitation
         *
         * @param id The id of the invitation
         */
        data class Deny(override val id: Long) : Invitations(id)

        /**
         * Permission to revoke a specific invitation
         *
         * @param id The id of the invitation
         */
        data class Revoke(override val id: Long) : Invitations(id)

    }

}