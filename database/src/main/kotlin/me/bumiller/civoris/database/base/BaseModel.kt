package me.bumiller.civoris.database.base

/**
 * Base class for any model
 */
interface BaseModel<Id: Comparable<Id>> {

    /**
     * The id of the model
     */
    val id: Id

}
