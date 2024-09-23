package me.bumiller.mol.model.http

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement

/**
 * Type of exception that is thrown for the sole purpose of automatically returning a specific HTTP-response to the user.
 */
data class RequestException(

    /**
     * The HTTP-status code
     */
    val code: Int = 400,

    /**
     * The body of the response. Must be json content.
     */
    val body: JsonElement?

) : RuntimeException(body.toString()) {

    companion object {

        inline fun <reified T : ErrorInfo> create(code: Int, errorType: String, info: T): RequestException {
            val body = ErrorWrapper(info, errorType)
            val jsonBody = try {
                Json.encodeToJsonElement(body)
            } catch (e: Exception) {
                null
            }

            return RequestException(code, jsonBody)
        }

    }

    /**
     * Error body for a [RequestException] for nice format of the HTTP-body.
     */
    @Serializable
    data class ErrorWrapper<T : ErrorInfo>(

        /**
         * More detailed description of the error.
         */
        val info: T?,

        /**
         * The type of error
         */
        val errorType: String

    )

}

/**
 * Will throw an [RequestException] with status 400.
 *
 * @param message Description of error
 */
fun bad(message: String? = null): Nothing {
    throw RequestException.create(400, "bad", ErrorInfo.DescriptionInfo(message ?: ""))
}

/**
 * Will throw an [RequestException] with status 400.
 *
 * @param format The expected format
 * @param value The value
 */
fun badFormat(format: String, value: String): Nothing {
    throw RequestException.create(
        code = 400,
        errorType = "bad_format",
        ErrorInfo.BadFormatInfo(format, value)
    )
}

/**
 * Will throw an [RequestException] with status 401.
 *
 * @param message Description of error
 */
fun unauthorized(message: String? = null): Nothing {
    throw RequestException.create(
        code = 401,
        errorType = "unauthorized",
        ErrorInfo.DescriptionInfo(message ?: "")
    )
}

/**
 * Will throw an [RequestException] with status 403.
 *
 * @param message Description of error
 */
fun forbidden(message: String? = null): Nothing {
    throw RequestException.create(
        code = 403,
        errorType = "forbidden",
        ErrorInfo.DescriptionInfo(message ?: "")
    )
}

/**
 * Will throw an [RequestException] with status 404.
 *
 * @param message Description of error
 */
fun notFound(message: String? = null): Nothing {
    throw RequestException.create(
        code = 404,
        errorType = "not_found",
        info = ErrorInfo.DescriptionInfo(message ?: "")
    )
}

/**
 * Will throw an [RequestException] with status 404.
 *
 * @param type The type of resource that was requested
 * @param identifier Identifier of the resource that was not found
 */
fun notFoundIdentifier(type: String, identifier: String): Nothing {
    throw RequestException.create(
        code = 404,
        errorType = "not_found_identifier",
        info = ErrorInfo.NotFoundIdentifierInfo(type, identifier)

    )
}

/**
 * Will throw an [RequestException] with status 409.
 *
 * @param message Description of error
 */
fun conflict(message: String? = null): Nothing {
    throw RequestException.create(
        code = 409,
        errorType = "conflict",
        info = ErrorInfo.DescriptionInfo(message ?: "")
    )
}

/**
 * Will throw an [RequestException] with status 409.
 *
 * @param field Field that caused a conflict
 * @param value Value of the field
 */
fun conflictUnique(field: String, value: String): Nothing {
    throw RequestException.create(
        code = 409,
        errorType = "conflict_unique",
        info = ErrorInfo.ConflictUniqueInfo(field, value)

    )
}

/**
 * Will throw an [RequestException] with status 500.
 */
fun internal() : Nothing {
    throw RequestException.create(
        code = 500,
        errorType = "internal",
        info = ErrorInfo.DescriptionInfo("")
    )
}

sealed class ErrorInfo {

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

    ) : ErrorInfo()

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

    ) : ErrorInfo()

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

    ) : ErrorInfo()

    /**
     * Error info for when a description is the only way to describe the error
     */
    @Serializable
    data class DescriptionInfo(

        /**
         * The description
         */
        val description: String

    ) : ErrorInfo()

}