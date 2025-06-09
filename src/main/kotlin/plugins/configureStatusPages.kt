package com.example.plugins

import com.example.data.model.ErrorResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureStatusPages() {
    install(StatusPages) {
        // Captura cualquier excepción genérica que no hayamos manejado específicamente
        exception<Throwable> { call, cause ->
            // En un entorno real, aquí registrarías el error en un sistema de logs
            // log.error("Unhandled error: ${cause.localizedMessage}", cause)

            // Devuelve una respuesta de error genérica y estandarizada
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("500: Error Interno del Servidor")
            )
        }

        // Captura errores específicos para dar respuestas más precisas
        exception<BadRequestException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(cause.message)
            )
        }

        // Puedes añadir más manejadores para otros tipos de excepciones...
        // exception<AuthenticationException> { ... }
        // exception<AuthorizationException> { ... }
    }
}

// Excepción personalizada para cuando Ktor no puede deserializar un cuerpo de petición
class BadRequestException(override val message: String = "Petición Inválida") : Exception(message)