package com.example.data.model

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class ProductVariant(
    val id: String,
    val productId: String,
    val sku: String? = null,
    val name: String? = null, // e.g., "Large, Red" or derived from attributes
    val price: Double,
    val stockQuantity: Int,
    val attributes: Map<String, String>? = null, // For JSONB attributes like {"color": "Red", "size": "L"}
    val imageUrl: String? = null,
    val createdAt: String? = null, // Will be populated by DB
    val updatedAt: String? = null  // Will be populated by DB
)

@Serializable
data class ProductVariantRequest(
    val sku: String? = null,
    val name: String? = null,
    val price: Double,
    val stockQuantity: Int,
    val attributes: Map<String, String>? = null,
    val imageUrl: String? = null
)
