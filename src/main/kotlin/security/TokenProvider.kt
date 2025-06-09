package security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.data.model.User
import io.ktor.server.config.*
import java.util.*

class TokenProvider(config: ApplicationConfig) {
    private val audience = config.property("jwt.audience").getString()
    private val secret = System.getenv("KTOR_JWT_SECRET") ?: config.property("jwt.secret").getString()
    private val issuer = config.property("jwt.issuer").getString()
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