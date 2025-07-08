package routes

import com.example.plugins.BadRequestException
import data.model.ReviewRequest
import data.repository.ReviewRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.reviewRouting() {
    val repository: ReviewRepository by inject()

    route("/products/{id}/reviews") {
        // GET /products/{id}/reviews - Public endpoint to get all reviews for a product
        get {
            val productId = call.parameters["id"] ?: throw BadRequestException("Product ID is missing.")
            val reviews = repository.getReviewsForProduct(productId)
            call.respond(reviews)
        }

        // POST /products/{id}/reviews - Protected endpoint to add a new review
        authenticate("auth-jwt") {
            post {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.getClaim("userId", String::class)!!
                // The user's name is denormalized to avoid an extra DB query.
                val userName = "${principal.getClaim("firstName", String::class)} ${principal.getClaim("lastName", String::class).orEmpty()}".trim()
                
                val productId = call.parameters["id"] ?: throw BadRequestException("Product ID is missing.")
                val request = call.receive<ReviewRequest>()

                val newReview = repository.addReviewForProduct(productId, userId, userName, request)
                call.respond(HttpStatusCode.Created, newReview)
            }
        }
    }
}