package me.bumiller.mol.rest.response.law.book

import kotlinx.serialization.SerialName
import me.bumiller.mol.model.LawBook

/**
 * Response class that contains information about a law-book
 */
data class LawBookResponse(

    val id: Long,

    val key: String,

    val name: String,

    val description: String,

    @SerialName("created_at")
    val creatorId: Long

) {

    companion object {

        fun create(lawBook: LawBook) = lawBook.run { LawBookResponse(id, key, name, description, creator.id) }

    }

}
