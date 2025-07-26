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
        // Route path updated from "/pedidos" to "/orders" for v2.0 consistency
        route("/orders") {

            /**
             * POST /orders/checkout
             * Initiates the checkout process. Creates a PENDING_PAYMENT order,
             * and prepares to return a payment URL for the frontend.
             */
            post("/checkout") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.getClaim("userId", String::class)!!
                val request = call.receive<CheckoutRequest>()

                // 1. Fetch the user's current cart
                val cart = cartRepository.getCart(userId)
                if (cart.items.isEmpty()) {
                    throw BadRequestException("Cannot checkout with an empty cart.")
                }

                // 2. Securely fetch the selected shipping address to ensure it belongs to the user
                val shippingAddress = addressRepository.findAddressByIdForUser(userId, request.addressId)
                    ?: throw NotFoundException("Selected address not found or does not belong to the user.")

                // 3. Create the order in the database with the fetched address
                val newOrder = orderRepository.createOrder(userId, cart.items, shippingAddress)

                // 4. Create Payment Preference with Mercado Pago
                // In a real implementation, you would make an API call here.
                // val preferenceRequest = MercadoPagoPreferenceRequest(
                //     items = newOrder.items.map { MercadoPagoItem(it.productName, it.quantity, it.unitPrice) },
                //     externalReference = newOrder.id, // This is the crucial link
                //     backUrls = mapOf("success" to "https://yourapp.com/success", "failure" to "https://yourapp.com/failure")
                // )
                // val preference = mercadoPagoService.createPreference(preferenceRequest)

                // For now, we continue to simulate the response.
                val mercadoPagoPreferenceId = "mp-pref-for-order-${newOrder.id}" // Simulated ID
                val mercadoPagoInitPoint =
                    "https://mercadopago.com.ar/checkout/v1/redirect?pref_id=$mercadoPagoPreferenceId"

                // It's good practice to save the preference ID with the order.
                // orderRepository.setMercadoPagoPreferenceId(newOrder.id, mercadoPagoPreferenceId)


                // 5. Clear the user's cart only after the order is successfully created
                cartRepository.clearCart(userId)

                // 6. Respond with the redirect URL for the payment provider
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
             * Retrieves details for a specific order, ensuring it belongs to the user.
             */
            get("{id}") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.getClaim("userId", String::class)!!
                val orderId = call.parameters["id"] ?: throw BadRequestException("Order ID is missing.")

                // Find the order within the user's own order list for security
                val order = orderRepository.getOrdersForUser(userId).find { it.id == orderId }
                    ?: throw NotFoundException("Order not found or you do not have permission to view it.")

                call.respond(order)
            }
        }
    }
}