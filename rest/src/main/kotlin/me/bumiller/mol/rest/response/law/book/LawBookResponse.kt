package me.bumiller.mol.rest.response.law.book

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.bumiller.mol.model.LawBook

/**
 * Response class that contains information about a law-book
 */
@Serializable
data class LawBookResponse(

    val id: Long,

    val key: String,

    val name: String,

    val description: String,

    @SerialName("creator_id")
    val creatorId: Long

) {

    companion object {

        fun create(lawBook: LawBook) = lawBook.run { LawBookResponse(id, key, name, description, creator.id) }

    }

}
