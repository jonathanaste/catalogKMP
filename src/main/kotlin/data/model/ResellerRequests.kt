package data.model

import kotlinx.serialization.Serializable

/**
 * DTO for an admin to create a new user and their reseller profile in one step.
 */
@Serializable
data class ResellerCreateRequest(
    val email: String,
    val firstName: String,
    val lastName: String,
    val phone: String?,
    val uniqueStoreSlug: String,
    val commissionRate: Double? = 20.0
)

/**
 * DTO for an admin to update an existing reseller's profile.
 */
@Serializable
data class ResellerUpdateRequest(
    val uniqueStoreSlug: String,
    val commissionRate: Double,
    val isActive: Boolean
)