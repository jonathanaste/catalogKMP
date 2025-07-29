package com.example.data.model
import kotlinx.serialization.Serializable

@Serializable
data class Product(
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
    val averageRating: Double = 0.0,
    val reviewCount: Int = 0,
    // Internal fields like supplierId, costPrice, isConsigned are handled
    // in the repository and not exposed in the main DTO.
)