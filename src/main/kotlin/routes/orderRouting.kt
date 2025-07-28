package com.example.routes

import com.example.data.repository.CartRepository
import com.example.data.repository.OrderRepository
import com.example.plugins.BadRequestException
import com.example.plugins.NotFoundException
import data.model.CheckoutRequest
import data.repository.AddressRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.orderRouting() {
    val orderRepository: OrderRepository by inject()
    val cartRepository: CartRepository by inject()
    val addressRepository: AddressRepository by inject()

    authenticate("auth-jwt") {
        route("/orders") {

            /**
             * POST /orders/checkout
             * Initiates the checkout process. Creates a PENDING_PAYMENT order,
             * applies an optional coupon, and prepares to return a payment URL.
             */
            post("/checkout") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.getClaim("userId", String::class)!!
                val request = call.receive<CheckoutRequest>() // This now contains the optional couponCode

                // 1. Fetch the user's current cart
                val cart = cartRepository.getCart(userId)
                if (cart.items.isEmpty()) {
                    throw BadRequestException("Cannot checkout with an empty cart.")
                }

                // 2. Securely fetch the selected shipping address
                val shippingAddress = addressRepository.findAddressByIdForUser(userId, request.addressId)
                    ?: throw NotFoundException("Selected address not found or does not belong to the user.")

                // 3. Create the order, passing the optional coupon code to the repository
                val newOrder = orderRepository.createOrder(
                    userId = userId,
                    cartItems = cart.items,
                    shippingAddress = shippingAddress,
                    couponCode = request.couponCode // <-- THE KEY CHANGE IS HERE
                )

                // 4. Simulate Mercado Pago preference creation
                val mercadoPagoPreferenceId = "mp-pref-${newOrder.id}"
                val mercadoPagoInitPoint = "https://mercadopago.com.ar/checkout/v1/redirect?pref_id=$mercadoPagoPreferenceId"

                // 5. Clear the user's cart
                cartRepository.clearCart(userId)

                // 6. Respond with the redirect URL
                call.respond(HttpStatusCode.Created, mapOf("init_point" to mercadoPagoInitPoint))
            }

            /**
             * GET /orders
             * Retrieves the authenticated user's order history.
             */
            get {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.getClaim("userId", String::class)!!
                val orders = orderRepository.getOrdersForUser(userId)
                call.respond(orders)
            }

            /**
             * GET /orders/{id}
             * Retrieves details for a specific order.
             */
            get("{id}") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.getClaim("userId", String::class)!!
                val orderId = call.parameters["id"] ?: throw BadRequestException("Order ID is missing.")

                val order = orderRepository.getOrdersForUser(userId).find { it.id == orderId }
                    ?: throw NotFoundException("Order not found or you do not have permission to view it.")

                call.respond(order)
            }
        }
    }
}