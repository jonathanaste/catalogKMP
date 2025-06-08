package com.example.data.repository

import com.example.data.model.*
import com.example.plugins.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.selectAll
import java.util.*

class OrderRepositoryImpl : OrderRepository {

    override suspend fun createOrder(userId: String, cartItems: List<ItemCarrito>): Pedido? {
        // dbQuery nos asegura que toda esta lógica se ejecute en una única transacción
        return dbQuery {
            if (cartItems.isEmpty()) return@dbQuery null

            // 1. Obtenemos los IDs de los productos del carrito
            val productIds = cartItems.map { it.productId }
            // 2. Buscamos esos productos en la DB para obtener sus precios y nombres ACTUALES
            val productsFromDb = ProductsTable
                .selectAll().where { ProductsTable.id inList productIds }
                .associateBy { it[ProductsTable.id] }

            // Verificamos que todos los productos del carrito sigan existiendo
            if (productIds.size != productsFromDb.size) {
                return@dbQuery null // Un producto fue eliminado mientras estaba en el carrito
            }

            var total = 0.0
            // 3. Creamos la lista de Items del Pedido, usando los datos frescos de la DB
            val orderItems = cartItems.map { cartItem ->
                val productData = productsFromDb[cartItem.productId]!!
                val currentPrice = productData[ProductsTable.price]
                total += currentPrice * cartItem.cantidad
                ItemPedido(
                    productId = cartItem.productId,
                    nombreProducto = productData[ProductsTable.name], // Guardamos el nombre actual
                    cantidad = cartItem.cantidad,
                    precioUnitario = currentPrice // Guardamos el precio actual
                )
            }

            // 4. Creamos la entrada principal del pedido en OrdersTable
            val newOrderId = UUID.randomUUID().toString()
            OrdersTable.insert {
                it[id] = newOrderId
                it[this.userId] = userId
                it[orderDate] = System.currentTimeMillis()
                it[status] = "PROCESANDO"
                it[this.total] = total
                it[paymentMethod] = "MERCADO_PAGO" // Valor por defecto para el MVP
                it[shippingMethod] = "CORREO_ARGENTINO" // Valor por defecto para el MVP
            }

            // 5. Insertamos todos los ítems del pedido en OrderItemsTable
            OrderItemsTable.batchInsert(orderItems) { item ->
                this[OrderItemsTable.orderId] = newOrderId
                this[OrderItemsTable.productId] = item.productId
                this[OrderItemsTable.productName] = item.nombreProducto
                this[OrderItemsTable.quantity] = item.cantidad
                this[OrderItemsTable.unitPrice] = item.precioUnitario
            }

            // 6. Construimos y devolvemos el objeto Pedido completo para la respuesta de la API
            Pedido(
                id = newOrderId,
                usuarioId = userId,
                fechaPedido = System.currentTimeMillis(),
                estado = "PROCESANDO",
                total = total,
                items = orderItems
            )
        }
    }

    override suspend fun getOrdersForUser(userId: String): List<Pedido> {
        return dbQuery {
            OrdersTable.selectAll().where { OrdersTable.userId eq userId }
                .orderBy(OrdersTable.orderDate, SortOrder.DESC)
                .map { resultRowToPedido(it) }
        }
    }

    // Función auxiliar para reconstruir un pedido desde la base de datos para el historial
    private fun resultRowToPedido(row: ResultRow): Pedido {
        val orderId = row[OrdersTable.id]
        // Por cada pedido, buscamos sus ítems correspondientes
        val items = OrderItemsTable
            .selectAll().where { OrderItemsTable.orderId eq orderId }
            .map {
                ItemPedido(
                    productId = it[OrderItemsTable.productId],
                    nombreProducto = it[OrderItemsTable.productName],
                    cantidad = it[OrderItemsTable.quantity],
                    precioUnitario = it[OrderItemsTable.unitPrice]
                )
            }
        return Pedido(
            id = orderId,
            usuarioId = row[OrdersTable.userId],
            fechaPedido = row[OrdersTable.orderDate],
            estado = row[OrdersTable.status],
            total = row[OrdersTable.total],
            items = items
        )
    }
}