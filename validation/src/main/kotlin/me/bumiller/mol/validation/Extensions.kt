package me.bumiller.mol.validation

import io.ktor.server.application.*
import io.ktor.server.request.*
import me.bumiller.mol.model.http.bad

/**
 * Extension function on [ApplicationCall] to wrap the parsing of the request-body that is a [Validatable] to also include the validation of that object.
 */
suspend inline fun <reified Body : Validatable> ApplicationCall.validated(): Body {
    return receiveOptional<Body>()?.apply {
        validate()
    } ?: bad()
}

/**
 * Extension function on [ApplicationCall] to return null if the body of a request could not be serialized into the requested format.
 */
suspend inline fun <reified Body : Any> ApplicationCall.receiveOptional(): Body? =
    try {
        receive<Body>()
    } catch (e: ContentTransformationException) {
        null
    }