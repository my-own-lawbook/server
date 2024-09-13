package me.bumiller.mol.rest.response.law.entry

import kotlinx.serialization.Serializable
import me.bumiller.mol.model.LawEntry

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
