package data.repository

import com.example.data.model.ItemCarrito
import com.example.data.model.ShoppingCart

interface CartRepository {
    suspend fun getCart(userId: String): ShoppingCart
    suspend fun addToCart(userId: String, item: ItemCarrito): ShoppingCart
    suspend fun removeFromCart(userId: String, productId: String): ShoppingCart
    suspend fun clearCart(userId: String): Boolean
}