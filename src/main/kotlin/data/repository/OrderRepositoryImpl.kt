package com.example.data.repository

import com.example.data.model.*
import com.example.plugins.BadRequestException
import com.example.plugins.ConflictException
import com.example.plugins.DatabaseFactory.dbQuery
import data.model.Address
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.selectAll
import java.util.*

class OrderRepositoryImpl : OrderRepository {

    override suspend fun createOrder(userId: String, cartItems: List<CartItem>, shippingAddress: Address): Order = dbQuery {
        if (cartItems.isEmpty()) throw BadRequestException("Cannot create an order from an empty cart.")

        val productIds = cartItems.map { it.productId }
        val productsFromDb = ProductsTable
            .selectAll().where { ProductsTable.id inList productIds }
            .associateBy { it[ProductsTable.id] }

        if (productIds.size != productsFromDb.size) {
            throw ConflictException("One or more products in the cart are no longer available.")
        }

        // Verify stock before processing the order
        for (cartItem in cartItems) {
            val productData = productsFromDb[cartItem.productId]
            if (productData == null || productData[ProductsTable.currentStock] < cartItem.quantity) {
                throw ConflictException("Insufficient stock for product with ID ${cartItem.productId}")
            }
        }

        var total = 0.0
        val orderItems = cartItems.map { cartItem ->
            val productData = productsFromDb[cartItem.productId]!!
            val currentPrice = productData[ProductsTable.price]
            total += currentPrice * cartItem.quantity
            OrderItem(
                productId = cartItem.productId,
                productName = productData[ProductsTable.name],
                quantity = cartItem.quantity,
                unitPrice = currentPrice
            )
        }

        val newOrderId = UUID.randomUUID().toString()
        val currentTime = System.currentTimeMillis()
        val newStatus = "PENDING_PAYMENT"

        // Create the main order entry
        OrdersTable.insert {
            it[id] = newOrderId
            it[this.userId] = userId
            it[orderDate] = currentTime
            it[status] = newStatus
            it[this.total] = total
            it[paymentMethod] = "MERCADO_PAGO" // Default for now
            it[shippingMethod] = "DEFAULT_SHIPPING" // Default for now
            it[this.shippingAddress] = Json.encodeToString(shippingAddress) // Serialize address to JSON string
        }

        // Insert all order items
        OrderItemsTable.batchInsert(orderItems) { item ->
            this[OrderItemsTable.orderId] = newOrderId
            this[OrderItemsTable.productId] = item.productId
            this[OrderItemsTable.productName] = item.productName
            this[OrderItemsTable.quantity] = item.quantity
            this[OrderItemsTable.unitPrice] = item.unitPrice
        }

        // Decrease product stock
        for (item in orderItems) {
            ProductsTable.update({ ProductsTable.id eq item.productId }) {
                with(SqlExpressionBuilder) {
                    it.update(currentStock, currentStock - item.quantity)
                }
            }
        }

        return@dbQuery Order(
            id = newOrderId,
            userId = userId,
            orderDate = currentTime,
            status = newStatus,
            total = total,
            shippingAddress = shippingAddress,
            paymentMethod = "MERCADO_PAGO",
            shippingMethod = "DEFAULT_SHIPPING",
            items = orderItems
        )
    }

    override suspend fun getOrdersForUser(userId: String): List<Order> {
        return dbQuery {
            OrdersTable.selectAll().where { OrdersTable.userId eq userId }
                .orderBy(OrdersTable.orderDate, SortOrder.DESC)
                .map { resultRowToOrder(it) }
        }
    }

    private fun resultRowToOrder(row: ResultRow): Order {
        val orderId = row[OrdersTable.id]
        val items = OrderItemsTable
            .selectAll().where { OrderItemsTable.orderId eq orderId }
            .map {
                OrderItem(
                    productId = it[OrderItemsTable.productId],
                    productName = it[OrderItemsTable.productName],
                    quantity = it[OrderItemsTable.quantity],
                    unitPrice = it[OrderItemsTable.unitPrice]
                )
            }

        // Deserialize the address from JSON string
        val addressJson = row[OrdersTable.shippingAddress]
        val address = addressJson?.let { Json.decodeFromString<Address>(it) }
            ?: throw IllegalStateException("Shipping address is missing for order $orderId")

        return Order(
            id = orderId,
            userId = row[OrdersTable.userId],
            orderDate = row[OrdersTable.orderDate],
            status = row[OrdersTable.status],
            total = row[OrdersTable.total],
            shippingAddress = address,
            paymentMethod = row[OrdersTable.paymentMethod],
            shippingMethod = row[OrdersTable.shippingMethod],
            mpPreferenceId = row[OrdersTable.mpPreferenceId],
            items = items
        )
    }
}