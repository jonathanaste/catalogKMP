package com.example.data.model
import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val mainImageUrl: String,
    val categoryId: String,
    val stockQuantity: Int,
    val supplierId: String?
)

@Serializable
data class ProductRequest(
    val name: String,
    val description: String,
    val price: Double,
    val mainImageUrl: String,
    val categoryId: String,
    val stockQuantity: Int,
    val supplierId: String?
)