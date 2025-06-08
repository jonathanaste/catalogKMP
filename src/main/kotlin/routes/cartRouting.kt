package com.example.routes

import com.example.data.model.ItemCarrito
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
        route("/carrito") {
            // Obtener el carrito del usuario actual
            get {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.getClaim("userId", String::class)!!

                val cart = repository.getCart(userId)
                call.respond(cart)
            }

            // Añadir un ítem al carrito
            post("/agregar") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.getClaim("userId", String::class)!!
                val item = call.receive<ItemCarrito>()

                val updatedCart = repository.addToCart(userId, item)
                call.respond(updatedCart)
            }

            // Quitar un ítem del carrito (usaremos un POST para simplicidad)
            post("/quitar") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.getClaim("userId", String::class)!!
                val itemToRemove = call.receive<ItemCarrito>() // Esperamos un objeto con el productId a quitar

                val updatedCart = repository.removeFromCart(userId, itemToRemove.productId)
                call.respond(updatedCart)
            }

            // Vaciar el carrito
            delete {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.getClaim("userId", String::class)!!

                repository.clearCart(userId)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}