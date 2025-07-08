package com.example.data.repository

import com.example.data.model.*
import com.example.plugins.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import java.util.*

class ProductRepositoryImpl : ProductRepository {

    /**
     * Converts a database ResultRow into a Product data class.
     * This helper handles the complex mapping, including the parsing of
     * PostgreSQL arrays into a List<String>.
     */
    private fun resultRowToProduct(row: ResultRow): Product {
        // Exposed doesn't have a native PG array type, so we get the raw string.
        // The format is "{url1,url2,url3}". We need to clean it up.
        val imageUrlsString = row.getOrNull(ProductsTable.additionalImageUrls) ?: "{}"
        val additionalImageUrls = imageUrlsString
            .removeSurrounding("{", "}") // Remove curly braces
            .split(',')                     // Split by comma
            .filter { it.isNotBlank() }     // Filter out empty strings if the array was empty

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

    override suspend fun getAllProducts(): List<Product> = dbQuery {
        ProductsTable.selectAll().map(::resultRowToProduct)
    }

    override suspend fun getProductById(id: String): Product? = dbQuery {
        ProductsTable
            .selectAll().where { ProductsTable.id eq id }
            .map(::resultRowToProduct)
            .singleOrNull()
    }

    override suspend fun addProduct(request: ProductRequest): Product {
        val newId = UUID.randomUUID().toString()
        return dbQuery {
            ProductsTable.insert {
                it[id] = newId
                it[sku] = request.sku
                it[name] = request.name
                it[description] = request.description
                it[price] = request.price
                it[salePrice] = request.salePrice
                it[mainImageUrl] = request.mainImageUrl
                it[additionalImageUrls] = "{${request.additionalImageUrls.joinToString(",")}}"
                it[categoryId] = request.categoryId
                it[currentStock] = request.currentStock
                it[weightKg] = request.weightKg
                it[supplierId] = request.supplierId
                it[costPrice] = request.costPrice
                it[isConsigned] = request.isConsigned
                // averageRating and reviewCount use database defaults
            }
            // After inserting, we fetch the complete product to return it
            getProductById(newId)!!
        }
    }

    override suspend fun updateProduct(id: String, request: ProductRequest): Boolean = dbQuery {
        ProductsTable.update({ ProductsTable.id eq id }) {
            it[sku] = request.sku
            it[name] = request.name
            it[description] = request.description
            it[price] = request.price
            it[salePrice] = request.salePrice
            it[mainImageUrl] = request.mainImageUrl
            it[additionalImageUrls] = "{${request.additionalImageUrls.joinToString(",")}}"
            it[categoryId] = request.categoryId
            it[currentStock] = request.currentStock
            it[weightKg] = request.weightKg
            it[supplierId] = request.supplierId
            it[costPrice] = request.costPrice
            it[isConsigned] = request.isConsigned
        } > 0
    }

    override suspend fun deleteProduct(id: String): Boolean = dbQuery {
        ProductsTable.deleteWhere { ProductsTable.id eq id } > 0
    }
}