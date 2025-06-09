package com.example.routes

import com.example.data.repository.CartRepository
import com.example.data.repository.OrderRepository
import com.example.plugins.BadRequestException
import io.ktor.http.*
import io.ktor.server.application.call
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import kotlin.getValue

fun Route.orderRouting() {
    val orderRepository: OrderRepository by inject()
    val cartRepository: CartRepository by inject()

    authenticate("auth-jwt") {
        route("/pedidos") {
            // POST /pedidos/checkout
            post("/checkout") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.getClaim("userId", String::class)!!

                val cart = cartRepository.getCart(userId)
                if (cart.items.isEmpty()) {
                    throw BadRequestException("El carrito está vacío.")
                }

                val newOrder = orderRepository.createOrder(userId, cart.items)

                cartRepository.clearCart(userId) // Vaciamos el carrito solo si el pedido fue exitoso
                call.respond(HttpStatusCode.Created, newOrder)
            }

            // GET /pedidos
            get {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.getClaim("userId", String::class)!!

                val orders = orderRepository.getOrdersForUser(userId)
                call.respond(orders)
            }
        }
    }
}