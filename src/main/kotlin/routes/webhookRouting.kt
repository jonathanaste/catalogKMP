package routes

import com.example.data.repository.OrderRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import services.MercadoPagoService

fun Route.webhookRouting() {
    val orderRepository: OrderRepository by inject()
    val mercadoPagoService: MercadoPagoService by inject()
    val log = LoggerFactory.getLogger("WebhookRouting")

    post("/webhooks/mercado-pago") {
        try {
            val notification = call.receive<Map<String, Any>>()
            log.info("Received Mercado Pago notification: $notification")

            val action = notification["action"] as? String
            val data = notification["data"] as? Map<*, *>
            val paymentId = data?.get("id") as? String

            if (action != "payment.updated" || paymentId == null) {
                call.respond(HttpStatusCode.BadRequest, "Notification is malformed.")
                return@post
            }

            // Securely fetch payment details from Mercado Pago API
            val paymentDetails = mercadoPagoService.getPaymentDetails(paymentId)

            if (paymentDetails == null) {
                log.warn("Could not retrieve details for paymentId: $paymentId")
                call.respond(HttpStatusCode.NotFound, "Payment not found.")
                return@post
            }

            val orderId = paymentDetails.externalReference
            if (orderId == null) {
                log.warn("Payment $paymentId is missing the external_reference (orderId).")
                call.respond(HttpStatusCode.BadRequest, "Missing external_reference.")
                return@post
            }

            // Update Order Status based on verified payment status
            when (paymentDetails.status) {
                "approved" -> {
                    orderRepository.updateOrderStatus(orderId, "PAID")
                    log.info("Order $orderId status updated to PAID for payment $paymentId.")
                }
                "rejected" -> {
                    orderRepository.updateOrderStatus(orderId, "REJECTED")
                    log.info("Order $orderId status updated to REJECTED for payment $paymentId.")
                }
                else -> {
                    log.info("Received unhandled payment status '${paymentDetails.status}' for order $orderId.")
                }
            }

            call.respond(HttpStatusCode.OK)

        } catch (e: Exception) {
            log.error("Error processing Mercado Pago webhook: ${e.message}", e)
            call.respond(HttpStatusCode.InternalServerError, "Error processing webhook.")
        }
    }
}