package com.example.plugins

import com.example.data.model.ErrorResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.slf4j.LoggerFactory

fun Application.configureStatusPages() {

    // Obtenemos una instancia del logger para este contexto específico
    val log = LoggerFactory.getLogger(Application::class.java)

    install(StatusPages) {
        // Manejador para nuestra excepción personalizada de 400
        exception<BadRequestException> { call, cause ->
            log.warn("Petición inválida: ${cause.message}")
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(cause.message))
        }

        // Manejador para nuestra excepción personalizada de 401
        exception<UnauthorizedException> { call, cause ->
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse(cause.message))
        }

        // Manejador para nuestra excepción personalizada de 404
        exception<NotFoundException> { call, cause ->
            call.respond(HttpStatusCode.NotFound, ErrorResponse(cause.message))
        }

        // Manejador para nuestra excepción personalizada de 403
        exception<ForbiddenException> { call, cause ->
            call.respond(HttpStatusCode.Forbidden, ErrorResponse(cause.message))
        }

        // Manejador para nuestra excepción personalizada de 409
        exception<ConflictException> { call, cause ->
            call.respond(HttpStatusCode.Conflict, ErrorResponse(cause.message))
        }

        exception<IllegalArgumentException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(cause.message ?: "Parámetro inválido"))
        }

        // Manejador genérico para cualquier otra excepción no controlada
        exception<Throwable> { call, cause ->
            log.error("Error no manejado: ${cause.localizedMessage}", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("500: Error Interno del Servidor")
            )
        }
    }
}

// Excepción personalizada para cuando Ktor no puede deserializar un cuerpo de petición
class BadRequestException(override val message: String = "Petición Inválida") : Exception(message)
class NotFoundException(override val message: String = "404: Recurso no encontrado") : Exception(message)
class UnauthorizedException(override val message: String = "401: No autorizado") : Exception(message)
class ForbiddenException(override val message: String = "403: Acceso denegado") : Exception(message)
class ConflictException(override val message: String = "409: Conflicto de recursos") : Exception(message)
