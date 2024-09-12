package me.bumiller.mol.database.table.crossref

import me.bumiller.mol.database.table.LawBook
import me.bumiller.mol.database.table.User
import org.jetbrains.exposed.sql.Table

object LawBookMembersCrossref : Table(name = "law_bow_members") {

    var lawBook = reference("law_book_id", LawBook.Table)

    val member = reference("user_id", User.Table)

    override val primaryKey = PrimaryKey(lawBook, member)

}