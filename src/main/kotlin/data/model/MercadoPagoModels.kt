// In src/main/kotlin/data/model/MercadoPagoModels.kt

package data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the relevant fields from the response of the
 * Mercado Pago Get Payment API (`v1/payments/{id}`).
 */
@Serializable
data class MercadoPagoPaymentResponse(
    val id: Long,
    val status: String, // e.g., "approved", "rejected"
    @SerialName("external_reference")
    val externalReference: String? = null
)

/**
 * Represents the body we would send to create a Payment Preference.
 * This is a simplified version for our use case.
 */
@Serializable
data class MercadoPagoPreferenceRequest(
    val items: List<MercadoPagoItem>,
    @SerialName("external_reference")
    val externalReference: String,
    @SerialName("back_urls")
    val backUrls: Map<String, String>
)

@Serializable
data class MercadoPagoItem(
    val title: String,
    val quantity: Int,
    @SerialName("unit_price")
    val unitPrice: Double
)