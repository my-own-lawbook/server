package me.bumiller.civoris.rest.response.law.section

import kotlinx.serialization.Serializable
import me.bumiller.civoris.model.LawSection

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
