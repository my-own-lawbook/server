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
fun bad(message: String? = null) {
    throw RequestException(400, DefaultErrorBody(message))
}

/**
 * Will throw an [RequestException] with status 401.
 *
 * @param message Description of error
 */
fun unauthorized(message: String? = null) {
    throw RequestException(401, DefaultErrorBody(message))
}

/**
 * Will throw an [RequestException] with status 403.
 *
 * @param message Description of error
 */
fun forbidden(message: String? = null) {
    throw RequestException(403, DefaultErrorBody(message))
}

/**
 * Will throw an [RequestException] with status 404.
 *
 * @param message Description of error
 */
fun notFound(message: String? = null) {
    throw RequestException(404, DefaultErrorBody(message))
}

/**
 * Will throw an [RequestException] with status 409.
 *
 * @param message Description of error
 */
fun conflict(message: String? = null) {
    throw RequestException(409, DefaultErrorBody(message))
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