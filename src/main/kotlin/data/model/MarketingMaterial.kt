package data.model

import kotlinx.serialization.Serializable

/**
 * Represents a marketing resource provided to resellers.
 */
@Serializable
data class MarketingMaterial(
    val id: String,
    val title: String,
    val description: String?,
    val assetUrl: String,
    val assetType: String,
    val dateAdded: Long
)

/**
 * Represents the request body for an admin creating a new marketing resource.
 * For now, we assume the admin provides a direct URL to the asset.
 */
@Serializable
data class MarketingMaterialRequest(
    val title: String,
    val description: String?,
    val assetUrl: String,
    val assetType: String // e.g., 'IMAGE', 'BANNER', 'PDF'
)