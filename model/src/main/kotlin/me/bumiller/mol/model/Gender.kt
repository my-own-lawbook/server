package me.bumiller.mol.model

import kotlinx.serialization.Serializable
import me.bumiller.mol.model.serialization.GenderSerializer

/**
 * Model for a gender selection made by the user
 */
@Serializable(with = GenderSerializer::class)
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