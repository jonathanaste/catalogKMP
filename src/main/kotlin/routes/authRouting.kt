package com.example.routes

import com.example.data.model.LoginRequest
import com.example.data.model.RegisterRequest
import com.example.data.repository.UserRepository
import io.ktor.http.*
import io.ktor.server.application.call
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import security.TokenProvider
import kotlin.getValue

fun Route.authRouting(tokenProvider: TokenProvider) {
    val repository: UserRepository by inject()

    route("/auth") {
        // POST /auth/register
        post("/register") {
            val request = call.receive<RegisterRequest>()
            val newUser = repository.registerUser(request)

            if (newUser != null) {
                call.respond(HttpStatusCode.Created, newUser)
            } else {
                call.respond(HttpStatusCode.Conflict, "El email ya está en uso.")
            }
        }

        // POST /auth/login
        post("/login") {
            val request = call.receive<LoginRequest>()

            // 1. Verificar que el usuario y la contraseña son correctos
            val isPasswordCorrect = repository.checkPassword(request.email, request.password)
            if (!isPasswordCorrect) {
                return@post call.respond(HttpStatusCode.Unauthorized, "Email o contraseña incorrectos.")
            }

            // 2. Si son correctos, encontrar los datos del usuario para generar el token
            val user = repository.findUserByEmail(request.email)
            if (user == null) {
                return@post call.respond(HttpStatusCode.InternalServerError, "Error inesperado del servidor.")
            }

            // 3. Generar y devolver el token
            val token = tokenProvider.generateToken(user)
            call.respond(HttpStatusCode.OK, mapOf("token" to token))
        }
    }
}