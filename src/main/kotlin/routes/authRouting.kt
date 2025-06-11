package com.example.routes

import com.example.data.model.LoginRequest
import com.example.data.model.RegisterRequest
import com.example.data.repository.UserRepository
import com.example.plugins.ConflictException
import com.example.plugins.UnauthorizedException
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
            val newUser = repository.registerUser(request) ?: throw ConflictException("El email ya está en uso.")
            call.respond(HttpStatusCode.Created, newUser)
        }

        // POST /auth/login
        post("/login") {
            val request = call.receive<LoginRequest>()

            // 1. Verificar que el usuario y la contraseña son correctos
            val isPasswordCorrect = repository.checkPassword(request.email, request.password)
            if (!isPasswordCorrect) {
                throw UnauthorizedException("Email o contraseña incorrectos.")
            }

            // 2. Si son correctos, encontrar los datos del usuario para generar el token
            val user =
                repository.findUserByEmail(request.email)!! //dado que la contraseña es correcta podemos forzar con !!

            // 3. Generar y devolver el token
            val token = tokenProvider.generateToken(user)
            // Ahora enviamos el token Y el rol del usuario.
            call.respond(
                HttpStatusCode.OK, mapOf(
                    "token" to token,
                    "rol" to user.role // Asegúrate de que tu clase User tiene la propiedad 'role'
                )
            )
        }
    }
}