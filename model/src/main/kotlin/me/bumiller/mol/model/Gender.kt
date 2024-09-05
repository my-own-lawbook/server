package me.bumiller.mol.model

/**
 * Model for a gender selection made by the user
 */
sealed interface Gender {

    /**
     * Male gender
     */
    data object Male: Gender

    /**
     * Female gender
     */
    data object Female: Gender

    /**
     * Other gender
     */
    data object Other: Gender

    /**
     * User does not want to disclose
     */
    data object Disclosed: Gender

}