package me.bumiller.mol.model

/**
 * Model for a gender selection made by the user
 */
sealed class Gender(

    val serializedName: String

) {

    /**
     * Male gender
     */
    data object Male: Gender("male")

    /**
     * Female gender
     */
    data object Female: Gender("female")

    /**
     * Other gender
     */
    data object Other: Gender("other")

    /**
     * User does not want to disclose
     */
    data object Disclosed: Gender("disclosed")

}