package me.bumiller.mol.common

/**
 * Custom implementation of [java.util.Optional] in a more kotlin-idiomatic way. This also explicitly allows for storing null values.
 *
 * @param T The type to encapsulate
 */
sealed class Optional<T> {

    /**
     * The value is present
     *
     * @param value The encapsulated value
     */
    data class Present<T>(internal val value: T) : Optional<T>()

    /**
     * The value is not present
     */
    class Empty<T> : Optional<T>()

    /**
     * Gets the value of this optional.
     *
     * @return The encapsulated value
     * @throws NoSuchElementException If the value is not present
     */
    fun get(): T = when (this) {
        is Empty -> throw NoSuchElementException("This optional instance is empty")
        is Present -> value
    }

    /**
     * Gets the value of this optional, or another default value
     *
     * @param default The default value
     */
    fun getOr(default: T): T = when (this) {
        is Empty -> default
        is Present -> value
    }

    /**
     * Gets the value or null
     *
     * @return The value or null
     */
    fun getOrNull(): T? = when (this) {
        is Empty -> null
        is Present -> value
    }

    /**
     * Gets whether the value is present
     */
    val isPresent: Boolean
        get() = this is Present

    override fun equals(other: Any?): Boolean = when (other) {
        is Optional<*> -> when (this) {
            is Empty -> when (other) {
                is Empty<*> -> true
                is Present<*> -> false
            }

            is Present -> when (other) {
                is Empty<*> -> false
                is Present<*> -> other.value == this.value
            }
        }

        else -> false
    }

}

/**
 * Creates an [Optional.Present] instance
 *
 * @param value The value to encapsulate
 * @return The optional
 */
fun <T> present(value: T): Optional<T> = Optional.Present(value)

/**
 * Returns an empty instance when the given [value] is null, else a present instance.
 *
 * @param value The passed value
 * @return [Optional.Empty] if [value] is null, else [Optional.Present] with [value]
 */
fun <T> presentWhenNotNull(value: T?): Optional<T> = when (value) {
    null -> Optional.Empty()
    else -> Optional.Present(value)
}

/**
 * Creates an [Optional.Empty] instance
 *
 * @return The optional
 */
fun <T> empty(): Optional<T> = Optional.Empty()