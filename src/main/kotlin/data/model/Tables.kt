package com.example.data.model

import org.jetbrains.exposed.sql.Table

// Tabla para Categorías, basada en tu data class Categoria
object CategoriesTable : Table("categories") {
    val id = varchar("id", 128)
    val name = varchar("name", 255)
    val imageUrl = varchar("image_url", 1024).nullable()

    override val primaryKey = PrimaryKey(id)
}

// Tabla para Productos, basada en tu data class Producto
object ProductsTable : Table("products") {
    val id = varchar("id", 128)
    val name = varchar("name", 255)
    val description = text("description")
    val price = double("price")
    val mainImageUrl = varchar("main_image_url", 1024)
    val categoryId = varchar("category_id", 128).references(CategoriesTable.id)
    val stockQuantity = integer("stock_quantity") // <-- LÍNEA NUEVA

    override val primaryKey = PrimaryKey(id)
}

// Tabla para Usuarios, basada en tu data class Usuario
object UsersTable : Table("users") {
    val id = varchar("id", 128)
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 512) // NUNCA guardes contraseñas en texto plano
    val name = varchar("name", 255)
    val role = varchar("role", 50) // "CLIENTE", "ADMIN"

    override val primaryKey = PrimaryKey(id)
}

// Tabla para Pedidos, basada en tu data class Pedido
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