package com.example.data.model

import data.model.Address
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.json.jsonb

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
    val sku = varchar("sku", 255).uniqueIndex() // <-- NEW
    val name = varchar("name", 255)
    val description = text("description")
    val price = double("price")
    val salePrice = double("sale_price").nullable() // <-- NEW
    val mainImageUrl = varchar("main_image_url", 1024)
    // For PostgreSQL array type, we can map it to a basic string in Exposed
    // and handle the conversion in the repository. A custom column type is also possible.
    val additionalImageUrls = text("additional_image_urls").nullable() // <-- NEW
    val categoryId = varchar("category_id", 128).references(CategoriesTable.id)
    val currentStock = integer("current_stock") // <-- Renamed
    val weightKg = double("weight_kg").nullable() // <-- NEW
    val averageRating = double("average_rating").default(0.0) // <-- NEW
    val reviewCount = integer("review_count").default(0) // <-- NEW
    val supplierId = varchar("supplier_id", 128).references(SuppliersTable.id).nullable()
    val costPrice = double("cost_price")
    val isConsigned = bool("is_consigned")
    override val primaryKey = PrimaryKey(id)
}

object UsersTable : Table("users") {
    val id = varchar("id", 128)
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 512)
    val firstName = varchar("first_name", 255) // <-- UPDATED
    val lastName = varchar("last_name", 255)  // <-- UPDATED
    val phone = varchar("phone", 50).nullable() // <-- NEW
    val role = varchar("role", 50) // e.g. "CLIENT", "ADMIN"

    override val primaryKey = PrimaryKey(id)
}

// --- NEW TABLE DEFINITION ---
object AddressesTable : Table("addresses") {
    val id = varchar("id", 128)
    val userId = varchar("user_id", 128).references(UsersTable.id)
    val alias = varchar("alias", 100)
    val street = varchar("street", 255)
    val number = varchar("number", 50)
    val postalCode = varchar("postal_code", 50)
    val city = varchar("city", 100)
    val state = varchar("state", 100)
    val isDefault = bool("is_default").default(false)

    override val primaryKey = PrimaryKey(id)
}

object OrdersTable : Table("orders") {
    val id = varchar("id", 128)
    val userId = varchar("user_id", 128).references(UsersTable.id)
    val orderDate = long("order_date")
    val status = varchar("status", 100) // e.g., "PENDING_PAYMENT"
    val total = double("total")
    val paymentMethod = varchar("payment_method", 100)
    val shippingMethod = varchar("shipping_method", 100)
    val shippingAddress = jsonb<Address>("shipping_address", Json).nullable()
    val mpPreferenceId = varchar("mp_preference_id", 255).nullable() // <-- NEW
    val couponCode = varchar("coupon_code", 100).references(CouponsTable.code).nullable()
    val discountAmount = double("discount_amount").nullable()
    val resellerId = varchar("reseller_id", 128).references(UsersTable.id).nullable()

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

object ProductReviewsTable : Table("product_reviews") {
    val id = varchar("id", 128)
    val productId = varchar("product_id", 128).references(ProductsTable.id)
    val userId = varchar("user_id", 128).references(UsersTable.id)
    val userName = varchar("user_name", 255)
    val rating = integer("rating")
    val title = varchar("title", 255)
    val comment = text("comment")
    val photoUrls = text("photo_urls").nullable() // Will be stored as a single delimited string
    val date = long("date")

    override val primaryKey = PrimaryKey(id)
}

object ProductQuestionsTable : Table("product_questions") {
    val id = varchar("id", 128)
    val productId = varchar("product_id", 128).references(ProductsTable.id)
    val userId = varchar("user_id", 128).references(UsersTable.id)
    val userName = varchar("user_name", 255)
    val questionText = text("question_text")
    val date = long("date")

    override val primaryKey = PrimaryKey(id)
}

object QuestionAnswersTable : Table("question_answers") {
    // A question can have only one answer, so questionId is the primary key.
    val questionId = varchar("question_id", 128).references(ProductQuestionsTable.id, onDelete = ReferenceOption.CASCADE)
    val answerText = text("answer_text")
    val date = long("date")

    override val primaryKey = PrimaryKey(questionId)
}

object WishlistItemsTable : Table("wishlist_items") {
    val userId = varchar("user_id", 128).references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val productId = varchar("product_id", 128).references(ProductsTable.id, onDelete = ReferenceOption.CASCADE)
    val dateAdded = long("date_added")

    override val primaryKey = PrimaryKey(userId, productId)
}

object CouponsTable : Table("coupons") {
    val code = varchar("code", 100)
    val description = text("description")
    val discountType = varchar("discount_type", 50)
    val discountValue = double("discount_value")
    val expirationDate = long("expiration_date").nullable()
    val usageLimit = integer("usage_limit").nullable()
    val usageCount = integer("usage_count")
    val isActive = bool("is_active")

    override val primaryKey = PrimaryKey(code)
}

object OrderCouponsTable : Table("order_coupons") {
    val orderId = varchar("order_id", 128).references(OrdersTable.id)
    val couponCode = varchar("coupon_code", 100).references(CouponsTable.code)
    val discountAmount = double("discount_amount")

    override val primaryKey = PrimaryKey(orderId, couponCode)
}

object ResellerProfilesTable : Table("reseller_profiles") {
    val userId = varchar("user_id", 128).references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val uniqueStoreSlug = varchar("unique_store_slug", 100).uniqueIndex()
    val commissionRate = double("commission_rate").default(20.0)
    val isActive = bool("is_active").default(true)

    override val primaryKey = PrimaryKey(userId)
}