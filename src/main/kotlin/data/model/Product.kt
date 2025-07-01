package com.example.data.model
import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: String,
    val name: String,
    val description: String,
    var price: Double, // Price on base product might be a default or average
    val mainImageUrl: String,
    val categoryId: String,
    var stockQuantity: Int, // Stock on base product might be sum of variants or a default
    val supplierId: String?,
    val costPrice: Double, // This might also move to variant level or be an average
    val isConsigned: Boolean,
    val hasVariants: Boolean = false,
    val variants: List<ProductVariant>? = null, // List of variants
    val createdAt: String? = null, // Assuming these might be added later or are already there
    val updatedAt: String? = null
)

@Serializable
data class ProductRequest(
    val name: String,
    val description: String,
    val price: Double, // Price for the base product or default variant
    val mainImageUrl: String,
    val categoryId: String,
    val stockQuantity: Int, // Stock for the base product or default variant
    val supplierId: String? = null,
    val costPrice: Double,
    val isConsigned: Boolean,
    val variants: List<ProductVariantRequest>? = null // Allow specifying variants during creation/update
)