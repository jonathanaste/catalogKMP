package com.example.routes

import com.example.data.model.CartItem
import com.example.data.repository.CartRepository
import io.ktor.http.*
import io.ktor.server.application.call
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.cartRouting() {
    val repository: CartRepository by inject()

    authenticate("auth-jwt") {
        // Standardized to English: "/cart"
        route("/cart") {
            // GET /cart - Get the current user's cart
            get {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.getClaim("userId", String::class)!!

                val cart = repository.getCart(userId)
                call.respond(cart)
            }

            // Standardized to English: "/cart/add"
            post("/add") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.getClaim("userId", String::class)!!
                val item = call.receive<CartItem>()

                val updatedCart = repository.addToCart(userId, item)
                call.respond(updatedCart)
            }

            // Standardized to English: "/cart/remove"
            post("/remove") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.getClaim("userId", String::class)!!
                // Expects a body with the productId to remove, e.g., {"productId": "some-id"}
                val itemToRemove = call.receive<CartItem>()

                val updatedCart = repository.removeFromCart(userId, itemToRemove.productId)
                call.respond(updatedCart)
            }

            // DELETE /cart - Clear the entire cart
            delete {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.getClaim("userId", String::class)!!

                repository.clearCart(userId)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}