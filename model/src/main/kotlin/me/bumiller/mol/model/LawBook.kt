package me.bumiller.mol.model

/**
 * Model for a law-book that represents a top entry in the law-hierarchy. A law-book is created by one user and can contain other users (= 'members').
 *
 * The children of a law-book are [LawEntry]s.
 */
data class LawBook(

    /**
     * The id
     */
    val id: Long,

    /**
     * The unique key, a shorthand of [name].
     */
    val key: String,

    /**
     * The name
     */
    val name: String,

    /**
     * The description
     */
    val description: String,

    /**
     * The user that created and owns this law-book.
     */
    val creator: User


)
