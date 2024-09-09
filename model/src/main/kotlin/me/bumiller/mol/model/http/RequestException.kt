package me.bumiller.mol.model.http

/**
 * Type of exception that is thrown for the sole purpose of automatically returning a specific HTTP-response to the user.
 */
data class RequestException(

    /**
     * The HTTP-status code
     */
    val code: Int = 400,

    /**
     * The body of the response. Will be serialized into JSON.
     */
    val body: Any

) : RuntimeException(body.toString())

/**
 * Will throw an [RequestException] with status 400.
 *
 * @param message Description of error
 */
fun bad(message: String? = null): Nothing {
    throw RequestException(400, DefaultErrorBody(message))
}

/**
 * Will throw an [RequestException] with status 400.
 *
 * @param format The expected format
 * @param value The value
 */
fun badFormat(format: String, value: String): Nothing {
    bad("Value '$value' violates the format '$format''")
}

/**
 * Will throw an [RequestException] with status 401.
 *
 * @param message Description of error
 */
fun unauthorized(message: String? = null): Nothing {
    throw RequestException(401, DefaultErrorBody(message))
}

/**
 * Will throw an [RequestException] with status 403.
 *
 * @param message Description of error
 */
fun forbidden(message: String? = null): Nothing {
    throw RequestException(403, DefaultErrorBody(message))
}

/**
 * Will throw an [RequestException] with status 404.
 *
 * @param message Description of error
 */
fun notFound(message: String? = null): Nothing {
    throw RequestException(404, DefaultErrorBody(message))
}

/**
 * Will throw an [RequestException] with status 404.
 *
 * @param type The type of resource that was requested
 * @param identifier Identifier of the resource that was not found
 */
fun notFoundIdentifier(type: String, identifier: String): Nothing {
    throw RequestException(404, DefaultErrorBody("Did not find $type with identifier '$identifier'"))
}

/**
 * Will throw an [RequestException] with status 409.
 *
 * @param message Description of error
 */
fun conflict(message: String? = null): Nothing {
    throw RequestException(409, DefaultErrorBody(message))
}

/**
 * Will throw an [RequestException] with status 409.
 *
 * @param field Field that caused a conflict
 * @param value Value of the field
 */
fun conflictUnique(field: String, value: String): Nothing {
    throw RequestException(409, DefaultErrorBody("Value '$value' is already taken for '$field'"))
}

/**
 * Will throw an [RequestException] with status 500.
 */
fun internal() : Nothing {
    throw RequestException(500, "")
}

/**
 * Default error body for a [RequestException] for nice format of the HTTP-body.
 */
data class DefaultErrorBody(

    /**
     * Description of the error that occurred.
     */
    val error: String?

) {

    override fun toString() = error ?: ""

}