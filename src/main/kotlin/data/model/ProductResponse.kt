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
    val inStock: Boolean,
    val category: Category // <-- Aquí está la magia: un objeto Category completo
)