package data.model

import kotlinx.serialization.Serializable

@Serializable
data class CheckoutRequest(
    val addressId: String,
    val couponCode: String? = null,
    val resellerSlug: String? = null
)