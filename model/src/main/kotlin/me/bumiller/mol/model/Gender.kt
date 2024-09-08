package me.bumiller.mol.model

/**
 * Model for a gender selection made by the user
 */
enum class Gender(

    val serializedName: String

) {

    /**
     * Male gender
     */
    Male("male"),

    /**
     * Female gender
     */
    Female("female"),

    /**
     * Other gender
     */
    Other("other"),

    /**
     * User does not want to disclose
     */
    Disclosed("disclosed")

}