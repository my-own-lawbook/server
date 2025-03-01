package me.bumiller.civoris.core.mapping

import me.bumiller.civoris.model.LawBook
import me.bumiller.civoris.model.LawEntry
import me.bumiller.civoris.model.LawSection
import me.bumiller.civoris.database.table.LawBook.Model as LawBookModel
import me.bumiller.civoris.database.table.LawEntry.Model as LawEntryModel
import me.bumiller.civoris.database.table.LawSection.Model as LawSectionModel

internal fun mapBook(book: LawBookModel) = LawBook(
    id = book.id,
    key = book.key,
    description = book.description,
    name = book.name,
    members = book.members.map { mapUser(it) }
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