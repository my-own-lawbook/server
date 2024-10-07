package me.bumiller.mol.model

/**
 * Model for a law-section that represents one specific aspect of a law. A law-section is bound to the [LawEntry] it is created in.
 */
data class LawSection(

    /**
     * The id
     */
    val id: Long,

    /**
     * The 'position' in the parent [LawEntry].
     *
     * Meant to be a number indicating the position in the parent [LawEntry], possibly with a suffix letter.
     */
    val index: String,

    /**
     * The name, which is a concise description of the topic this section deals with.
     */
    val name: String,

    /**
     * The content of the section.
     *
     * May contain markup.
     */
    val content: String


)
