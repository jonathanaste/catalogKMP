package data.model

import kotlinx.serialization.Serializable

/**
 * Represents a customer's information from the perspective of a reseller.
 */
@Serializable
data class ResellerCustomer(
    val customerId: String,
    val customerName: String,
    val customerEmail: String,
    val firstPurchaseDate: Long,
    val totalSpent: Double
)