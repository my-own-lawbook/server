package me.bumiller.mol.validation

/**
 * Interface for any data-class that can have their contents validated.
 */
interface Validatable {

    /**
     * Called when the object will be validated.
     */
    suspend fun validate() {
        // Empty so that implementations can leave this method empty
    }

}