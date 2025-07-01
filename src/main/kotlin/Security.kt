package com.example

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt

fun Application.configureSecurity() {
    val secret = System.getenv("KTOR_JWT_SECRET")
        ?: throw RuntimeException("Missing environment variable KTOR_JWT_SECRET")
    val issuer = System.getenv("KTOR_JWT_ISSUER")
        ?: throw RuntimeException("Missing environment variable KTOR_JWT_ISSUER")
    val audience = System.getenv("KTOR_JWT_AUDIENCE")
        ?: throw RuntimeException("Missing environment variable KTOR_JWT_AUDIENCE")
    val realm = System.getenv("KTOR_JWT_REALM")
        ?: throw RuntimeException("Missing environment variable KTOR_JWT_REALM")

    install(Authentication) {
        jwt("auth-jwt") {
            this.realm = realm
            verifier(
                JWT.require(Algorithm.HMAC256(secret))
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("userId").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}
