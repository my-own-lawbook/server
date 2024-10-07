package me.bumiller.mol.database.base

/**
 * Indicates that an entity has a related model it can provide and receive attributes from.
 */
interface ModelMappableEntity<Model> {

    /**
     * The model representation of this entity
     */
    val asModel: Model

    /**
     * Copies the applicable attributes from the [model] into the entity.
     *
     * If [Model] contains references to other objects, that correspond to database relations in this entity, the id's of those objects are used to populate the fields of this entity.
     *
     * @param model The model to take the attributes from.
     */
    fun populate(model: Model)

}