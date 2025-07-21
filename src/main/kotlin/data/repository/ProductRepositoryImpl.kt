package com.example.data.repository

import com.example.data.model.*
import com.example.plugins.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import java.util.*

class ProductRepositoryImpl : ProductRepository {

    private val delimiter = ","

    private fun resultRowToProduct(row: ResultRow): Product {
        // CORRECTED: Read the single string and split it into a list.
        val urlsString = row.getOrNull(ProductsTable.additionalImageUrls)
        val additionalImageUrls = urlsString?.split(delimiter)?.filter { it.isNotBlank() } ?: emptyList()

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
        val createdProduct = Product(
            id = newId,
            sku = request.sku,
            name = request.name,
            description = request.description,
            price = request.price,
            salePrice = request.salePrice,
            mainImageUrl = request.mainImageUrl,
            additionalImageUrls = request.additionalImageUrls,
            categoryId = request.categoryId,
            currentStock = request.currentStock,
            weightKg = request.weightKg,
            averageRating = 0.0, // Defaults
            reviewCount = 0      // Defaults
        )

        dbQuery {
            ProductsTable.insert {
                it[id] = createdProduct.id
                it[sku] = createdProduct.sku
                it[name] = createdProduct.name
                it[description] = createdProduct.description
                it[price] = createdProduct.price
                it[salePrice] = createdProduct.salePrice
                it[mainImageUrl] = createdProduct.mainImageUrl
                // CORRECTED: Join the list into a simple delimited string.
                it[additionalImageUrls] = createdProduct.additionalImageUrls.joinToString(delimiter)
                it[categoryId] = createdProduct.categoryId
                it[currentStock] = createdProduct.currentStock
                it[weightKg] = createdProduct.weightKg
                it[supplierId] = request.supplierId
                it[costPrice] = request.costPrice
                it[isConsigned] = request.isConsigned
            }
        }
        return createdProduct
    }

    override suspend fun updateProduct(id: String, request: ProductRequest): Boolean = dbQuery {
        ProductsTable.update({ ProductsTable.id eq id }) {
            it[sku] = request.sku
            it[name] = request.name
            it[description] = request.description
            it[price] = request.price
            it[salePrice] = request.salePrice
            it[mainImageUrl] = request.mainImageUrl
            // CORRECTED: Join the list into a simple delimited string.
            it[additionalImageUrls] = request.additionalImageUrls.joinToString(delimiter)
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