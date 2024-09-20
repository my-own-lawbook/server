package me.bumiller.mol.rest.response.law.book

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

    val description: String

) {

    companion object {

        fun create(lawBook: LawBook) = lawBook.run { LawBookResponse(id, key, name, description) }

    }

}
