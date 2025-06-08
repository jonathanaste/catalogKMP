package com.example

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt

fun Application.configureSecurity() {
    // Instalamos el plugin de Autenticación
    install(Authentication) {
        // Configuramos el proveedor de autenticación JWT que llamaremos "auth-jwt"
        jwt("auth-jwt") {
            // "realm" es un nombre para el área protegida, se usa en las respuestas
            realm = this@configureSecurity.environment.config.property("jwt.realm").getString()

            // "verifier" se encarga de comprobar que el token sea válido (no esté caducado,
            // la firma sea correcta, el issuer y audience coincidan, etc.)
            verifier(
                JWT.require(Algorithm.HMAC256(this@configureSecurity.environment.config.property("jwt.secret").getString()))
                    .withAudience(this@configureSecurity.environment.config.property("jwt.audience").getString())
                    .withIssuer(this@configureSecurity.environment.config.property("jwt.issuer").getString())
                    .build()
            )

            // "validate" es el último paso. Una vez que el token es verificado,
            // este bloque decide si el contenido (payload) es válido para la sesión.
            validate { credential ->
                // Extraemos el userId del payload del token. Si existe y no está vacío,
                // consideramos el token válido y creamos un objeto JWTPrincipal.
                if (credential.payload.getClaim("userId").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null // Si no, el token no es válido.
                }
            }
        }
    }
}
