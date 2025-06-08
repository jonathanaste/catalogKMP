package com.example.routes

import data.repository.CartRepository
import data.repository.CartRepositoryImpl
import data.repository.OrderRepository
import data.repository.OrderRepositoryImpl
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.orderRouting() {
    val orderRepository: OrderRepository = OrderRepositoryImpl()
    val cartRepository: CartRepository = CartRepositoryImpl()

    authenticate("auth-jwt") {
        route("/pedidos") {
            // POST /pedidos/checkout
            post("/checkout") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.getClaim("userId", String::class)!!

                val cart = cartRepository.getCart(userId)
                if (cart.items.isEmpty()) {
                    return@post call.respond(HttpStatusCode.BadRequest, "El carrito está vacío.")
                }

                val newOrder = orderRepository.createOrder(userId, cart.items)

                if (newOrder != null) {
                    cartRepository.clearCart(userId) // Vaciamos el carrito solo si el pedido fue exitoso
                    call.respond(HttpStatusCode.Created, newOrder)
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Error al crear el pedido.")
                }
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