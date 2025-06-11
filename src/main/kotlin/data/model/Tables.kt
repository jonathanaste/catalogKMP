package com.example.data.model

import org.jetbrains.exposed.sql.Table


object SuppliersTable : Table("suppliers") {
    val id = varchar("id", 128)
    val name = varchar("name", 255)
    val contactPerson = varchar("contact_person", 255).nullable()
    val phone = varchar("phone", 50).nullable()
    val email = varchar("email", 255).uniqueIndex().nullable()
    val cbu = varchar("cbu", 22).nullable()
    val aliasCbu = varchar("alias_cbu", 100).nullable()
    val notes = text("notes").nullable()

    override val primaryKey = PrimaryKey(id)
}

object CategoriesTable : Table("categories") {
    val id = varchar("id", 128)
    val name = varchar("name", 255)
    val imageUrl = varchar("image_url", 1024).nullable()

    override val primaryKey = PrimaryKey(id)
}

object ProductsTable : Table("products") {
    val id = varchar("id", 128)
    val name = varchar("name", 255)
    val description = text("description")
    val price = double("price")
    val mainImageUrl = varchar("main_image_url", 1024)
    val categoryId = varchar("category_id", 128).references(CategoriesTable.id)
    val stockQuantity = integer("stock_quantity")
    val supplierId = varchar("supplier_id", 128).references(SuppliersTable.id).nullable() // <-- AÑADE ESTA LÍNEA
    val costPrice = double("cost_price")
    val isConsigned = bool("is_consigned")
    override val primaryKey = PrimaryKey(id)
}

object UsersTable : Table("users") {
    val id = varchar("id", 128)
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 512) // NUNCA guardes contraseñas en texto plano
    val name = varchar("name", 255)
    val role = varchar("role", 50) // "CLIENTE", "ADMIN"

    override val primaryKey = PrimaryKey(id)
}

// Tabla para Pedidos, basada en data class Pedido
object OrdersTable : Table("orders") {
    val id = varchar("id", 128)
    val userId = varchar("user_id", 128).references(UsersTable.id)
    val orderDate = long("order_date")
    val status = varchar("status", 100) // Ej. "PENDIENTE_PAGO", "PROCESANDO"
    val total = double("total")
    val paymentMethod = varchar("payment_method", 100)
    val shippingMethod = varchar("shipping_method", 100)

    override val primaryKey = PrimaryKey(id)
}

// Tabla para los ítems de un pedido, basada en ItemPedido
object OrderItemsTable : Table("order_items") {
    val id = integer("id").autoIncrement()
    val orderId = varchar("order_id", 128).references(OrdersTable.id)
    val productId = varchar("product_id", 128).references(ProductsTable.id)
    val quantity = integer("quantity")
    val unitPrice = double("unit_price") // Precio al momento de la compra
    val productName = varchar("product_name", 255) // Copia del nombre por si cambia en el catálogo

    override val primaryKey = PrimaryKey(id)
}

// Tabla para los ítems del carrito de compras
object CartItemsTable : Table("cart_items") {
    val id = integer("id").autoIncrement()
    val userId = varchar("user_id", 128).references(UsersTable.id)
    val productId = varchar("product_id", 128).references(ProductsTable.id)
    val quantity = integer("quantity")

    override val primaryKey = PrimaryKey(id)
    // Creamos un índice único para asegurar que no haya productos duplicados por usuario
    init {
        uniqueIndex(userId, productId)
    }
}