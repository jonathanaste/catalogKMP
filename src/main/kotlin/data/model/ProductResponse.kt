package com.example.data.model

import kotlinx.serialization.Serializable

/**
 * Represents the rich product object sent to the client.
 * It's aligned with the v2.0 technical document and includes the full category details
 * for a more convenient frontend integration.
 */
@Serializable
data class ProductResponse(
    // Core Product Details
    val id: String,
    val sku: String,
    val name: String,
    val description: String,

    // Pricing
    val price: Double,
    val salePrice: Double? = null,

    // Media and Stock
    val mainImageUrl: String,
    val additionalImageUrls: List<String> = emptyList(),
    val currentStock: Int,

    // Shipping and Social Proof
    val weightKg: Double? = null,
    val averageRating: Double,
    val reviewCount: Int,

    // --- Embedded Object ---
    // Instead of just a categoryId, we provide the full object.
    val category: Category
)