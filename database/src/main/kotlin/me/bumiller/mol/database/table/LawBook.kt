package me.bumiller.mol.database.table

import me.bumiller.mol.database.base.BaseModel
import me.bumiller.mol.database.base.ModelMappableEntity
import me.bumiller.mol.database.table.crossref.LawBookMembersCrossref
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

        val creator: User.Model,

        val members: List<User.Model>

    ) : BaseModel<Long>

    object Table : LongIdTable(name = "law_book") {

        val key = text("key").uniqueIndex()

        val name = text("name")

        val description = text("description")

        val creator = reference("creator_id", User.Table)

    }

    internal class Entity(id: EntityID<Long>) : LongEntity(id), ModelMappableEntity<Long, Model> {

        var key by Table.key
        var name by Table.name
        var description by Table.description
        var creator by User.Entity referencedOn Table.creator

        var members by User.Entity via LawBookMembersCrossref

        override fun populate(model: Model) {
            key = model.key
            name = model.name
            description = model.description
            creator = User.Entity.findById(model.creator.id)!!
            members = User.Entity.forIds(model.members.map(BaseModel<Long>::id))
        }

        override val asModel: Model
            get() = Model(id.value, key, name, description, creator.asModel, members.map { it.asModel })

        companion object : LongEntityClass<Entity>(Table)

    }

}