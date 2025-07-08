package com.example.data.repository

import com.example.data.model.CartItem
import com.example.data.model.ShoppingCart

interface CartRepository {
    suspend fun getCart(userId: String): ShoppingCart
    suspend fun addToCart(userId: String, item: CartItem): ShoppingCart
    suspend fun removeFromCart(userId: String, productId: String): ShoppingCart
    suspend fun clearCart(userId: String): Boolean
}