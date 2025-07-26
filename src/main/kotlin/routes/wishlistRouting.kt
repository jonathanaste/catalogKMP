package routes

import data.model.AddToWishlistRequest
import data.repository.WishlistRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.wishlistRouting() {
    val repository: WishlistRepository by inject()

    authenticate("auth-jwt") {
        route("/wishlist") {

            /**
             * GET /wishlist
             * Retrieves the authenticated user's complete wishlist with full product details.
             */
            get {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.getClaim("userId", String::class)!!

                val wishlistItems = repository.getWishlistForUser(userId)
                call.respond(wishlistItems)
            }

            /**
             * POST /wishlist/add
             * Adds a single product to the authenticated user's wishlist.
             */
            post("/add") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.getClaim("userId", String::class)!!
                val request = call.receive<AddToWishlistRequest>()

                val newItem = repository.addToWishlist(userId, request.productId)
                if (newItem != null) {
                    call.respond(HttpStatusCode.Created, newItem)
                } else {
                    // If the item already exists, we can return a 200 OK or 409 Conflict.
                    // A simple OK is often sufficient.
                    call.respond(HttpStatusCode.OK, "Item is already in the wishlist.")
                }
            }

            /**
             * POST /wishlist/remove
             * Removes a single product from the authenticated user's wishlist.
             */
            post("/remove") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.getClaim("userId", String::class)!!
                val request = call.receive<AddToWishlistRequest>()

                val removed = repository.removeFromWishlist(userId, request.productId)
                if (removed) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    // This can happen if the item was not in the wishlist to begin with.
                    call.respond(HttpStatusCode.NotFound, "Item not found in wishlist.")
                }
            }
        }
    }
}