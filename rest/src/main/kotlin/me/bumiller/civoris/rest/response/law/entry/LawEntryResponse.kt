package me.bumiller.civoris.rest.response.law.entry

import kotlinx.serialization.Serializable
import me.bumiller.civoris.model.LawEntry

/**
 * Response class that contains information about one law-entry
 */
@Serializable
data class LawEntryResponse(

    val id: Long,

    val key: String,

    val name: String

) {

    companion object {

        fun create(entry: LawEntry) = entry.run { LawEntryResponse(id, key, name) }

    }

}
