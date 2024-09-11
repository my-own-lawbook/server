package me.bumiller.mol.database.table

import me.bumiller.mol.database.base.BaseModel
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object LawBook {

    data class Model(

        override val id: Long,

        val key: String,

        val name: String,

        val description: String,

        val creator: User.Model

    ) : BaseModel<Long>

    object Table : LongIdTable(name = "law_book") {

        val key = text("key").uniqueIndex()

        val name = text("name")

        val description = text("description")

        val creator = reference("creator_id", User.Table)

    }

    internal class Entity(id: EntityID<Long>) : LongEntity(id) {

        var key by Table.key
        var name by Table.name
        var description by Table.description
        var creator by User.Entity referencedOn Table.creator

        fun populate(model: Model, creatorEntity: User.Entity) {
            key = model.key
            name = model.name
            description = model.description
            creator = creatorEntity
        }

        val asModel: Model
            get() = Model(id.value, key, name, description, creator.asModel)

        companion object : LongEntityClass<Entity>(Table)

    }

}