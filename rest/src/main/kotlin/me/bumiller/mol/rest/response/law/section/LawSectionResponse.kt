package me.bumiller.mol.rest.response.law.section

import me.bumiller.mol.model.LawSection

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
