package me.bumiller.mol.core.mapping

import me.bumiller.mol.model.LawBook
import me.bumiller.mol.model.LawEntry
import me.bumiller.mol.model.LawSection
import me.bumiller.mol.database.table.LawBook.Model as LawBookModel
import me.bumiller.mol.database.table.LawEntry.Model as LawEntryModel
import me.bumiller.mol.database.table.LawSection.Model as LawSectionModel

internal fun mapBook(book: LawBookModel) = LawBook(
    id = book.id,
    key = book.key,
    description = book.description,
    name = book.name,
    creator = mapUser(book.creator)
)

internal fun mapEntry(entry: LawEntryModel) = LawEntry(
    id = entry.id,
    key = entry.key,
    name = entry.name
)

internal fun mapSection(section: LawSectionModel) = LawSection(
    id = section.id,
    index = section.index,
    name = section.name,
    content = section.content
)