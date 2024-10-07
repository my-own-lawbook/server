package me.bumiller.mol.validation

/**
 * Interface for any data-class that can have their contents validated.
 */
@SuppressWarnings("kotlin:S6517")
interface Validatable {

    /**
     * Called when the object will be validated.
     */
    @SuppressWarnings("kotlin:S6318")
    suspend fun validate() {
        // Empty so that implementations can leave this method empty
    }

}