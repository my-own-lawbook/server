package me.bumiller.mol.database.table.crossref

import me.bumiller.mol.database.base.ModelMappableEntity
import me.bumiller.mol.database.table.LawBook
import me.bumiller.mol.database.table.User
import org.jetbrains.exposed.dao.CompositeEntity
import org.jetbrains.exposed.dao.CompositeEntityClass
import org.jetbrains.exposed.dao.id.CompositeID
import org.jetbrains.exposed.dao.id.CompositeIdTable
import org.jetbrains.exposed.dao.id.EntityID

object LawBookMembersCrossref {

    data class Model(

        val bookId: Long,

        val userId: Long,

        val role: String

    )

    enum class Roles(
        val serializedName: String
    ) {

        Admin("admin"),

        Write("write"),

        Update("update"),

        Read("read")

    }

    object Table : CompositeIdTable(name = "law_bow_members") {

        var lawBook = reference("law_book_id", LawBook.Table)

        val member = reference("user_id", User.Table)

        val role = text("role").check("role-valid") {
            it inList Roles.entries.map(Roles::serializedName)
        }.default(Roles.Read.serializedName)

        override val primaryKey = PrimaryKey(lawBook, member)

    }

    internal class Entity(id: EntityID<CompositeID>) : CompositeEntity(id), ModelMappableEntity<Model> {

        var book by LawBook.Entity referencedOn Table.lawBook
        var member by User.Entity referencedOn Table.member
        var role by Table.role

        override val asModel = Model(book.id.value, member.id.value, role)

        override fun populate(model: Model) {
            book = LawBook.Entity.findById(model.bookId)!!
            member = User.Entity.findById(model.userId)!!
            role = model.role
        }

        companion object EntityClass : CompositeEntityClass<Entity>(Table)

    }

}