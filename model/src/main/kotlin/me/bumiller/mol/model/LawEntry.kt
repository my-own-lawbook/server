package me.bumiller.mol.model

/**
 * Model for a law-entry that represents one law dealing with a specific topic. A law-entry is bound to the [LawBook] it is created in.
 *
 * The children of a law-entry are [LawSection]s.
 */
data class LawEntry(

    /**
     * The id
     */
    val id: Long,

    /**
     * The unique key, supposed to be a shorthand of [name]
     */
    val key: String,

    /**
     * The name
     */
    val name: String

)
