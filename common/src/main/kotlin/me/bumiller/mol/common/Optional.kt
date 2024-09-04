package me.bumiller.mol.common

/**
 * Helper class that is similar to a java optional but allows for null to be set
 *
 * @param T The type that is kept in this box class
 */
class NullableOptional<T> private constructor(

    private val value: T?,

    private val isPresent: Boolean

) {

    /**
     * Returns the value if it is present or throws an exception when it is empty
     *
     * @return The value
     * @throws IllegalStateException If the value is not present
     */
    fun value(): T? =
        if (isPresent) value else throw IllegalStateException("Tried to get the value of an empty NullableOptional!")

    /**
     * Whether the value is present
     */
    fun isPresent(): Boolean = isPresent

    companion object {

        /**
         * The empty instance
         */
        val empty = NullableOptional(null, false)

        /**
         * Creates a [NullableOptional] that has a value
         */
        fun <T> present(value: T?) = NullableOptional(value, true)

        /**
         * Creates a [NullableOptional] off a nullable value. When [value] is null, [NullableOptional.empty] is returned, else a present instance.
         */
        fun <T> ofNullable(value: T?) = if (value == null) empty else NullableOptional(value, false)

    }

}