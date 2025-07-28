package com.example.data.repository

import com.example.data.model.*
import com.example.plugins.BadRequestException
import com.example.plugins.ConflictException
import com.example.plugins.DatabaseFactory.dbQuery
import data.model.Address
import data.model.Coupon
import data.repository.CouponRepository
import org.jetbrains.exposed.sql.*
import java.util.*
import kotlin.math.max

class OrderRepositoryImpl(
    private val couponRepository: CouponRepository
) : OrderRepository {

    override suspend fun createOrder(
        userId: String,
        cartItems: List<CartItem>,
        shippingAddress: Address,
        couponCode: String?,
        resellerId: String?
    ): Order = dbQuery {
        if (cartItems.isEmpty()) throw BadRequestException("Cannot create an order from an empty cart.")

        val productIds = cartItems.map { it.productId }
        val productsFromDb = ProductsTable
            .selectAll().where { ProductsTable.id inList productIds }
            .associateBy { it[ProductsTable.id] }

        if (productIds.size != productsFromDb.size) {
            throw ConflictException("One or more products in the cart are no longer available.")
        }

        var subtotal = 0.0
        for (cartItem in cartItems) {
            val productData = productsFromDb[cartItem.productId]
                ?: throw ConflictException("Product with ID ${cartItem.productId} not found.")
            if (productData[ProductsTable.currentStock] < cartItem.quantity) {
                throw ConflictException("Insufficient stock for product: ${productData[ProductsTable.name]}")
            }
            subtotal += (productData[ProductsTable.salePrice] ?: productData[ProductsTable.price]) * cartItem.quantity
        }

        var finalTotal = subtotal
        var discountAmount = 0.0
        var validatedCoupon: Coupon? = null

        if (!couponCode.isNullOrBlank()) {
            val coupon = couponRepository.findActiveCouponByCode(couponCode)
                ?: throw BadRequestException("The coupon code '$couponCode' is not valid or has expired.")

            discountAmount = when (coupon.discountType) {
                "PERCENTAGE" -> subtotal * (coupon.discountValue / 100.0)
                "FIXED_AMOUNT" -> coupon.discountValue
                else -> 0.0
            }
            finalTotal = max(0.0, subtotal - discountAmount)
            validatedCoupon = coupon
        }

        val orderItems = cartItems.map { cartItem ->
            val productData = productsFromDb[cartItem.productId]!!
            OrderItem(
                productId = cartItem.productId,
                productName = productData[ProductsTable.name],
                quantity = cartItem.quantity,
                unitPrice = (productData[ProductsTable.salePrice] ?: productData[ProductsTable.price])
            )
        }

        val newOrderId = UUID.randomUUID().toString()
        val currentTime = System.currentTimeMillis()

        OrdersTable.insert {
            it[id] = newOrderId
            it[this.userId] = userId
            it[orderDate] = currentTime
            it[status] = "PENDING_PAYMENT"
            it[total] = finalTotal
            it[paymentMethod] = "MERCADO_PAGO"
            it[shippingMethod] = "DEFAULT_SHIPPING"
            it[this.shippingAddress] = shippingAddress
            it[this.couponCode] = validatedCoupon?.code
            it[this.discountAmount] = discountAmount
            it[this.resellerId] = resellerId
        }

        validatedCoupon?.let { coupon ->
            CouponsTable.update({ CouponsTable.code eq coupon.code }) {
                with(SqlExpressionBuilder) {
                    it.update(usageCount, usageCount + 1)
                }
            }
            OrderCouponsTable.insert {
                it[this.orderId] = newOrderId
                it[this.couponCode] = coupon.code
                it[this.discountAmount] = discountAmount
            }
        }

        OrderItemsTable.batchInsert(orderItems) { item ->
            this[OrderItemsTable.orderId] = newOrderId
            this[OrderItemsTable.productId] = item.productId
            this[OrderItemsTable.productName] = item.productName
            this[OrderItemsTable.quantity] = item.quantity
            this[OrderItemsTable.unitPrice] = item.unitPrice
        }

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
            status = "PENDING_PAYMENT",
            total = finalTotal,
            shippingAddress = shippingAddress,
            paymentMethod = "MERCADO_PAGO",
            shippingMethod = "DEFAULT_SHIPPING",
            mpPreferenceId = null,
            items = orderItems,
            couponCode = validatedCoupon?.code,
            discountAmount = discountAmount,
            resellerId = resellerId
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

        val address = row[OrdersTable.shippingAddress]
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
            items = items,
            couponCode = row[OrdersTable.couponCode],
            discountAmount = row[OrdersTable.discountAmount],
            resellerId = row[OrdersTable.resellerId]
        )
    }

    override suspend fun updateOrderStatus(orderId: String, newStatus: String): Boolean = dbQuery {
        OrdersTable.update({ OrdersTable.id eq orderId }) {
            it[status] = newStatus
        } > 0
    }
}