package data.model

import kotlinx.serialization.Serializable

/**
 * Represents the response for a reseller fetching their personal store details.
 */
@Serializable
data class ResellerStoreResponse(
    val resellerName: String,
    val uniqueStoreSlug: String,
    val storeUrl: String, // The full, shareable URL for their store
    val commissionRate: Double,
    val isActive: Boolean
)