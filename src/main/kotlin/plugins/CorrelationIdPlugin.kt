package com.example.plugins

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.util.*
import org.slf4j.MDC
import java.util.*

const val CORRELATION_ID_HEADER = "X-Request-ID"
const val CORRELATION_ID_MDC_KEY = "correlationId"

val CorrelationIdPlugin = createApplicationPlugin(name = "CorrelationIdPlugin") {
    onCall { call ->
        val correlationId = call.request.header(CORRELATION_ID_HEADER) ?: UUID.randomUUID().toString()
        MDC.put(CORRELATION_ID_MDC_KEY, correlationId)
        call.response.headers.append(CORRELATION_ID_HEADER, correlationId)

        try {
            // Proceed with the call handling
        } finally {
            // Ensure MDC is cleared after the call is processed
            MDC.remove(CORRELATION_ID_MDC_KEY)
        }
    }
}
