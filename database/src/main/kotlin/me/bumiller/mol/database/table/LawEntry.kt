package me.bumiller.mol.database.table

import me.bumiller.mol.database.base.BaseModel
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object LawEntry {

    data class Model(

        override val id: Long,

        val key: String,

        val name: String


    ) : BaseModel<Long>

    object Table : LongIdTable(name = "law_entry") {

        val key = text("key").uniqueIndex()

        val name = text("name")

        val parentBook = reference("book_id", LawBook.Table)

        init {
            index(true, key, parentBook)
        }

    }

    internal class Entity(id: EntityID<Long>) : LongEntity(id) {

        var key by Table.key
        var name by Table.name
        var parentBook by LawBook.Entity referencedOn Table.parentBook

        fun populate(model: Model, parentBook: LawBook.Entity) {
            key = model.key
            name = model.name
            this.parentBook = parentBook
        }

        val asModel: Model
            get() = Model(id.value, key, name)

        companion object : LongEntityClass<Entity>(Table)

    }

}