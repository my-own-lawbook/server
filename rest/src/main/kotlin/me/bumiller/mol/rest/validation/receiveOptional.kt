package me.bumiller.mol.rest.validation

import io.ktor.server.application.*
import io.ktor.server.request.*

/**
 * Tries to parse the [ApplicationCall] into [Body], and returns null if it cannot
 */
internal suspend inline fun <reified Body : Any> ApplicationCall.receiveOptional(): Body? =
    try {
        receive<Body>()
    } catch (e: ContentTransformationException) {
        null
    }