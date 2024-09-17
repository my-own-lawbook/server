package me.bumiller.mol.rest.response.law.section

import kotlinx.serialization.Serializable
import me.bumiller.mol.model.LawSection

@Serializable
data class LawSectionResponse(

    val id: Long,

    val index: String,

    val name: String,

    val content: String

) {

    companion object {

        fun create(section: LawSection) = section.run { LawSectionResponse(id, index, name, content) }

    }

}
