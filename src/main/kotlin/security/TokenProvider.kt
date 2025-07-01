package security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.data.model.User
import java.util.*

class TokenProvider {
    private val audience = System.getenv("KTOR_JWT_AUDIENCE")
        ?: throw RuntimeException("Missing environment variable KTOR_JWT_AUDIENCE")
    private val secret = System.getenv("KTOR_JWT_SECRET")
        ?: throw RuntimeException("Missing environment variable KTOR_JWT_SECRET")
    private val issuer = System.getenv("KTOR_JWT_ISSUER")
        ?: throw RuntimeException("Missing environment variable KTOR_JWT_ISSUER")
    private val expirationDate = 36_000_00 * 24 // 24 horas en milisegundos

    fun generateToken(user: User): String {
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("email", user.email)
            .withClaim("userId", user.id)
            .withClaim("role", user.role)
            .withExpiresAt(Date(System.currentTimeMillis() + expirationDate))
            .sign(Algorithm.HMAC256(secret))
    }
}