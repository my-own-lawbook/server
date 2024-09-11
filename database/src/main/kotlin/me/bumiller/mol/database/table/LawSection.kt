package me.bumiller.mol.database.table

import me.bumiller.mol.database.base.BaseModel
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object LawSection {

    data class Model(

        override val id: Long,

        val index: String,

        val name: String,

        val content: String

    ) : BaseModel<Long>

    object Table : LongIdTable(name = "law_section") {

        val index = text("index")

        val name = text("name")

        val content = text("content")

        val parentEntry = reference("entry_id", LawEntry.Table)

        init {
            index(true, index, parentEntry)
        }

    }

    internal class Entity(id: EntityID<Long>) : LongEntity(id) {

        var index by Table.index
        var name by Table.name
        var content by Table.content
        var parentEntry by LawEntry.Entity referencedOn Table.parentEntry

        fun populate(model: Model, parentEntry: LawEntry.Entity) {
            index = model.index
            name = model.name
            content = model.content
            this.parentEntry = parentEntry
        }

        val asModel: Model
            get() = Model(id.value, index, name, content)

        companion object : LongEntityClass<Entity>(Table)

    }

}