package com.example.data.model
import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: String,
    val sku: String,                      // <-- NEW
    val name: String,
    val description: String,
    val price: Double,
    val salePrice: Double? = null,        // <-- NEW
    val mainImageUrl: String,
    val additionalImageUrls: List<String> = emptyList(), // <-- NEW
    val categoryId: String,
    val currentStock: Int,                // <-- Renamed from stockQuantity
    val weightKg: Double? = null,         // <-- NEW
    val averageRating: Double = 0.0,      // <-- NEW
    val reviewCount: Int = 0,             // <-- NEW
    // Internal fields like supplierId, costPrice, isConsigned are handled
    // in the repository and not exposed in the main DTO.
)