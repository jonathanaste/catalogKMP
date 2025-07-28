package data.model

import kotlinx.serialization.Serializable

/**
 * Represents the data associated with a RESELLER role.
 */
@Serializable
data class ResellerProfile(
    val userId: String,
    val uniqueStoreSlug: String,
    val commissionRate: Double,
    val isActive: Boolean
)