package me.bumiller.civoris.rest.util

import io.ktor.server.application.*
import io.ktor.util.pipeline.*
import me.bumiller.civoris.common.lazyWithReceiver
import me.bumiller.civoris.rest.plugins.authenticatedUser

/**
 * Utility extension property that delegates the first call to [authenticatedUser] and caches the result.
 */
internal val PipelineContext<*, ApplicationCall>.user by lazyWithReceiver { call.authenticatedUser() }