package me.bumiller.mol.model

/**
 * Model for a gender selection made by the user
 */
sealed class Gender {

    /**
     * Male gender
     */
    data object Male

    /**
     * Female gender
     */
    data object Female

    /**
     * Other gender
     */
    data object Other

    /**
     * User does not want to disclose
     */
    data object Disclosed

}