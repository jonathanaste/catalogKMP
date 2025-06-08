package data.repository

import com.example.data.model.ItemCarrito
import com.example.data.model.Pedido

interface OrderRepository {
    suspend fun createOrder(userId: String, cartItems: List<ItemCarrito>): Pedido?
    suspend fun getOrdersForUser(userId: String): List<Pedido>
}