package com.example.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CartItem(
    val productId: String,
    val quantity: Int
    // Nota: No guardamos el precio aquí. Lo buscaremos en la DB al momento del checkout
    // para asegurarnos de que sea el precio actual, una práctica de seguridad importante.
)

@Serializable
data class ShoppingCart(
    val items: List<CartItem>
)