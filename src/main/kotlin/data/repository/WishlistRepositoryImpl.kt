package data.repository

import com.example.data.model.Product
import com.example.data.model.ProductsTable
import com.example.data.model.WishlistItemsTable
import com.example.plugins.DatabaseFactory.dbQuery
import data.model.WishlistItem
import data.model.WishlistResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class WishlistRepositoryImpl : WishlistRepository {

    // Helper function to convert a result row to a Product object.
    // We'll need this to build our WishlistResponse.
    private fun resultRowToProduct(row: ResultRow): Product {
        val urlsString = row.getOrNull(ProductsTable.additionalImageUrls)
        val additionalImageUrls = urlsString?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
        return Product(
            id = row[ProductsTable.id],
            sku = row[ProductsTable.sku],
            name = row[ProductsTable.name],
            description = row[ProductsTable.description],
            price = row[ProductsTable.price],
            salePrice = row[ProductsTable.salePrice],
            mainImageUrl = row[ProductsTable.mainImageUrl],
            additionalImageUrls = additionalImageUrls,
            categoryId = row[ProductsTable.categoryId],
            currentStock = row[ProductsTable.currentStock],
            weightKg = row[ProductsTable.weightKg],
            averageRating = row[ProductsTable.averageRating],
            reviewCount = row[ProductsTable.reviewCount]
        )
    }


    override suspend fun getWishlistForUser(userId: String): List<WishlistResponse> = dbQuery {
        // --- CORRECTED JOIN SYNTAX ---
        // The join condition is provided directly after the tables are specified.
        (WishlistItemsTable innerJoin ProductsTable)
            .selectAll().where { (WishlistItemsTable.userId eq userId) and (WishlistItemsTable.productId eq ProductsTable.id) }
            .orderBy(WishlistItemsTable.dateAdded, SortOrder.DESC)
            .map {
                WishlistResponse(
                    product = resultRowToProduct(it),
                    dateAdded = it[WishlistItemsTable.dateAdded]
                )
            }
    }
    override suspend fun addToWishlist(userId: String, productId: String): WishlistItem? = dbQuery {
        // Check if the item already exists to avoid a unique constraint violation.
        val existingItem = WishlistItemsTable
            .selectAll()
            .where { (WishlistItemsTable.userId eq userId) and (WishlistItemsTable.productId eq productId) }
            .count()

        if (existingItem > 0) {
            return@dbQuery null // Already in wishlist
        }

        val currentTime = System.currentTimeMillis()
        WishlistItemsTable.insert {
            it[this.userId] = userId
            it[this.productId] = productId
            it[this.dateAdded] = currentTime
        }

        WishlistItem(userId, productId, currentTime)
    }

    override suspend fun removeFromWishlist(userId: String, productId: String): Boolean = dbQuery {
        WishlistItemsTable.deleteWhere {
            (this.userId eq userId) and (this.productId eq productId)
        } > 0
    }
}