package me.bumiller.mol.common

/**
 * Returns this list with non-null elements if none of the elements were null, or null
 */
inline fun <reified T> List<T?>.allNonNullOrNull(): List<T>? =
    listOfNotNull(*this.toTypedArray()).let { list ->
        if (list.size != size) null
        else list
    }