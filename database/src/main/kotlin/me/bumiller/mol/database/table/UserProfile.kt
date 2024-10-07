package me.bumiller.mol.database.table

import kotlinx.datetime.LocalDate
import me.bumiller.mol.database.base.BaseModel
import me.bumiller.mol.database.base.ModelMappableEntity
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.date

object UserProfile {

    /**
     * Model that represents a row in the 'user_profile' table
     */
    data class Model(

        /**
         * The id
         */
        override val id: Long,

        /**
         * The birthday
         */
        val birthday: LocalDate,

        /**
         * The first name
         */
        val firstName: String,

        /**
         * The last name
         */
        val lastName: String,

        /**
         * The gender
         */
        val gender: String

    ) : BaseModel<Long>

    object Table : LongIdTable("user_profile") {

        val birthday = date("birthday")

        val firstName = text("first_name")

        val lastName = text("last_name")

        val gender = text("gender")

    }

    internal class Entity(id: EntityID<Long>) : LongEntity(id), ModelMappableEntity<Model> {

        var birthday by Table.birthday
        var firstName by Table.firstName
        var lastName by Table.lastName
        var gender by Table.gender

        override val asModel
            get() = Model(id.value, birthday, firstName, lastName, gender)

        override fun populate(model: Model) {
            birthday = model.birthday
            firstName = model.firstName
            lastName = model.lastName
            gender = model.gender
        }

        companion object : LongEntityClass<Entity>(Table)

    }

}


