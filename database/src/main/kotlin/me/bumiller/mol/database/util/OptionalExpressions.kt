package me.bumiller.mol.database.util

import me.bumiller.mol.common.Optional
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

/**
 * Helper SQL-Operator that checks a column value against an optional value. If the optional is empty, true is returned.
 *
 * @param matcher THe optional
 * @return The operator
 */
@JvmName("eqOpt")
internal infix fun <T> Column<T>.eqOpt(matcher: Optional<T>) =
    if (matcher.isPresent) this eq matcher.get()
    else Op.TRUE

/**
 * Helper SQL-Operator that checks a column value against an optional value. If the optional is empty, true is returned.
 *
 * @param matcher THe optional
 * @return The operator
 */
@JvmName("eqOptId")
internal infix fun <T : Comparable<T>> Column<EntityID<T>>.eqOpt(matcher: Optional<T>) =
    if (matcher.isPresent) this eq matcher.get()
    else Op.TRUE