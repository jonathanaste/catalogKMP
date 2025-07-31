package data.model

import kotlinx.serialization.Serializable

@Serializable
data class AdminProductResponse(
    val id: String,
    val sku: String,
    val name: String,
    val description: String,
    val price: Double,
    val salePrice: Double? = null,
    val mainImageUrl: String,
    val additionalImageUrls: List<String> = emptyList(),
    val categoryId: String,
    val currentStock: Int,
    val weightKg: Double? = null,
    val supplierId: String?,
    val costPrice: Double,
    val isConsigned: Boolean
)