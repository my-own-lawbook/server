package me.bumiller.mol.database.table

import me.bumiller.mol.database.base.BaseModel
import me.bumiller.mol.database.base.ModelMappableEntity
import me.bumiller.mol.database.table.crossref.LawBookMembersCrossref
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column

object User {

    /**
     * Model that represents a row in the 'user' table
     */
    data class Model(

        /**
         * The id
         */
        override val id: Long,

        /**
         * The email, unique
         */
        val email: String,

        /**
         * The username, unique
         */
        val username: String,

        /**
         * The hashed password
         */
        val password: String,

        /**
         * Whether the user has had their email verified
         */
        val isEmailVerified: Boolean,

        /**
         * The profile
         */
        val profile: UserProfile.Model?

    ) : BaseModel<Long>

    object Table : LongIdTable("user") {

        val email: Column<String> = varchar("email", 255)
            .uniqueIndex()

        val username: Column<String> = varchar("username", 255)
            .uniqueIndex()

        val password: Column<String> = varchar("password", 255)

        val isEmailVerified = bool("email_verified").default(false)

        val profileId = reference("profile_id", UserProfile.Table).nullable()
    }

    internal class Entity(id: EntityID<Long>) : LongEntity(id), ModelMappableEntity<Long, Model> {

        var email by Table.email
        var username by Table.username
        var password by Table.password
        var isEmailVerified by Table.isEmailVerified
        var profile by UserProfile.Entity optionalReferencedOn Table.profileId

        var lawBooks by LawBook.Entity via LawBookMembersCrossref

        override val asModel
            get() = Model(id.value, email, username, password, isEmailVerified, profile?.asModel)

        override fun populate(model: Model) {
            email = model.email
            username = model.username
            password = model.password
            isEmailVerified = model.isEmailVerified
            profile = model.profile?.let { UserProfile.Entity.findById(it.id)!! }
        }

        companion object : LongEntityClass<Entity>(Table)

    }

}