package com.example.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ProductRequest(
    val sku: String, // <-- NEW
    val name: String,
    val description: String,
    val price: Double,
    val salePrice: Double? = null, // <-- NEW
    val mainImageUrl: String,
    val additionalImageUrls: List<String> = emptyList(), // <-- NEW
    val categoryId: String,
    val currentStock: Int, // <-- Renamed
    val weightKg: Double? = null, // <-- NEW
    val supplierId: String?,
    val costPrice: Double,
    val isConsigned: Boolean,
)