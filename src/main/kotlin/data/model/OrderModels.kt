package com.example.data.model

import data.model.Address
import kotlinx.serialization.Serializable

/**
 * Represents a single item within a completed order.
 * Based on the v2.0 Technical Document.
 */
@Serializable
data class OrderItem(
    val productId: String,
    val productName: String, // A snapshot of the product name at the time of purchase
    val quantity: Int,
    val unitPrice: Double // The price of the item at the time of purchase
)

/**
 * Represents a customer's completed order.
 * Aligned with the v2.0 Technical Document.
 */
@Serializable
data class Order(
    val id: String,
    val userId: String,
    val orderDate: Long, // UNIX Timestamp
    val status: String,
    val total: Double,
    val shippingAddress: Address,
    val paymentMethod: String,
    val shippingMethod: String,
    val mpPreferenceId: String? = null,
    val items: List<OrderItem>,
    val couponCode: String? = null,
    val discountAmount: Double? = null,
    val resellerId: String? = null
)