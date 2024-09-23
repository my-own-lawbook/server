package me.bumiller.mol.model.http

import kotlinx.serialization.Serializable

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
    throw RequestException(400, DefaultErrorBody(errorType = "bad", info = message))
}

/**
 * Will throw an [RequestException] with status 400.
 *
 * @param format The expected format
 * @param value The value
 */
fun badFormat(format: String, value: String): Nothing {
    throw RequestException(400, DefaultErrorBody(errorType = "bad_format", info = BadFormatInfo(format, value)))
}

/**
 * Will throw an [RequestException] with status 401.
 *
 * @param message Description of error
 */
fun unauthorized(message: String? = null): Nothing {
    throw RequestException(401, DefaultErrorBody(errorType = "unauthorized", info = message))
}

/**
 * Will throw an [RequestException] with status 403.
 *
 * @param message Description of error
 */
fun forbidden(message: String? = null): Nothing {
    throw RequestException(403, DefaultErrorBody(errorType = "forbidden", info = message))
}

/**
 * Will throw an [RequestException] with status 404.
 *
 * @param message Description of error
 */
fun notFound(message: String? = null): Nothing {
    throw RequestException(
        404, DefaultErrorBody(
            errorType = "not_found",
            info = message
        )
    )
}

/**
 * Will throw an [RequestException] with status 404.
 *
 * @param type The type of resource that was requested
 * @param identifier Identifier of the resource that was not found
 */
fun notFoundIdentifier(type: String, identifier: String): Nothing {
    throw RequestException(
        404, DefaultErrorBody(
            errorType = "not_found",
            info = NotFoundIdentifierInfo(type, identifier)
        )
    )
}

/**
 * Will throw an [RequestException] with status 409.
 *
 * @param message Description of error
 */
fun conflict(message: String? = null): Nothing {
    throw RequestException(
        409, DefaultErrorBody(
            errorType = "conflict",
            info = message
        )
    )
}

/**
 * Will throw an [RequestException] with status 409.
 *
 * @param field Field that caused a conflict
 * @param value Value of the field
 */
fun conflictUnique(field: String, value: String): Nothing {
    throw RequestException(
        409, DefaultErrorBody(
            errorType = "conflict_unique",
            info = ConflictUniqueInfo(field, value)
        )
    )
}

/**
 * Will throw an [RequestException] with status 500.
 */
fun internal() : Nothing {
    throw RequestException(500, "")
}

/**
 * Error body for a 409 response.
 */
@Serializable
data class ConflictUniqueInfo(

    /**
     * The field that already has the value set
     */
    val field: String,

    /**
     * The value of the field
     */
    val value: String

)

/**
 * Error body for a 404 response
 */
@Serializable
data class NotFoundIdentifierInfo(

    /**
     * The name of the resource type
     */
    val resourceType: String,

    /**
     * The identifier that was searched by
     */
    val identifier: String

)

/**
 * Error body for 400 when a format rule was violated
 */
@Serializable
data class BadFormatInfo(

    /**
     * The required format
     */
    val format: String,

    /**
     * The passed value
     */
    val value: String

)

/**
 * Default error body for a [RequestException] for nice format of the HTTP-body.
 */
@Serializable
data class DefaultErrorBody<T>(

    /**
     * More detailed description of the error.
     */
    val info: T,

    /**
     * The type of error
     */
    val errorType: String? = null

)