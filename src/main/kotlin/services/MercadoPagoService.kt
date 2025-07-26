// In src/main/kotlin/services/MercadoPagoService.kt

package services

import data.model.MercadoPagoPaymentResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class MercadoPagoService(private val accessToken: String) {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    private val apiUrl = "https://api.mercadopago.com/v1"

    /**
     * Fetches the details of a specific payment from the Mercado Pago API.
     * @param paymentId The ID of the payment to retrieve.
     * @return A [MercadoPagoPaymentResponse] object or null if not found or an error occurs.
     */
    suspend fun getPaymentDetails(paymentId: String): MercadoPagoPaymentResponse? {
        return try {
            val response = client.get("$apiUrl/payments/$paymentId") {
                bearerAuth(accessToken)
                contentType(ContentType.Application.Json)
            }
            if (response.status == HttpStatusCode.OK) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            // Log the exception in a real application
            null
        }
    }
}