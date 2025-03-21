package me.bumiller.civoris.rest.response.law.book

import kotlinx.serialization.Serializable
import me.bumiller.civoris.model.LawBook

/**
 * Response class that contains information about a law-book
 */
@Serializable
data class LawBookResponse(

    val id: Long,

    val key: String,

    val name: String,

    val description: String,

    val isMemberOf: Boolean

) {

    companion object {

        fun createForMember(lawBook: LawBook) = create(lawBook, true)

        fun createForInvited(lawBook: LawBook) = create(lawBook, false)

        fun create(lawBook: LawBook, isMember: Boolean) =
            lawBook.run { LawBookResponse(id, key, name, description, isMember) }

    }

}
