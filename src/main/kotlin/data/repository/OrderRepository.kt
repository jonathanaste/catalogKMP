package com.example.data.repository

import com.example.data.model.CartItem
import com.example.data.model.Order
import data.model.Address

interface OrderRepository {
    /**
     * Creates a new order from the user's cart items, selected shipping address,
     * and an optional discount coupon.
     * @param userId The ID of the user placing the order.
     * @param cartItems The list of items from the user's shopping cart.
     * @param shippingAddress The user's selected shipping address.
     * @param couponCode The optional coupon code to apply to the order.
     * @return The newly created Order.
     */
    suspend fun createOrder(
        userId: String,
        cartItems: List<CartItem>,
        shippingAddress: Address,
        couponCode: String? // <-- ADD THIS PARAMETER
    ): Order

    /**
     * Retrieves the order history for a specific user.
     * @param userId The ID of the user.
     * @return A list of the user's past Orders.
     */
    suspend fun getOrdersForUser(userId: String): List<Order>

    suspend fun updateOrderStatus(orderId: String, newStatus: String): Boolean
}