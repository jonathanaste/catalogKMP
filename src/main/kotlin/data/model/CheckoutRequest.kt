package data.model

import kotlinx.serialization.Serializable

@Serializable
data class CheckoutRequest(
    val addressId: String
)