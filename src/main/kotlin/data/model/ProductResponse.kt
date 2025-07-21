package com.example.data.model

import kotlinx.serialization.Serializable

// Este objeto representará la respuesta completa que enviará nuestra API
@Serializable
data class ProductResponse(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val mainImageUrl: String,
    val currentStock: Int,
    val supplierId: String?,
    val costPrice: Double,
    val isConsigned: Boolean,
    val category: Category // <-- Aquí está la magia: un objeto Category completo
)