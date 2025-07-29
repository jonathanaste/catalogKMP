package com.example.data.repository

import com.example.data.model.*
import com.example.plugins.BadRequestException
import com.example.plugins.DatabaseFactory.dbQuery
import data.model.ProductSummaryResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import java.util.*

class ProductRepositoryImpl : ProductRepository {

    private val delimiter = ","

    private fun resultRowToProductResponse(row: ResultRow): ProductResponse {
        val category = Category(
            id = row[CategoriesTable.id],
            name = row[CategoriesTable.name],
            imageUrl = row[CategoriesTable.imageUrl]
        )
        val urlsString = row.getOrNull(ProductsTable.additionalImageUrls)
        val additionalImageUrls = urlsString?.split(delimiter)?.filter { it.isNotBlank() } ?: emptyList()
        return ProductResponse(
            id = row[ProductsTable.id],
            sku = row[ProductsTable.sku],
            name = row[ProductsTable.name],
            description = row[ProductsTable.description],
            price = row[ProductsTable.price],
            salePrice = row[ProductsTable.salePrice],
            mainImageUrl = row[ProductsTable.mainImageUrl],
            additionalImageUrls = additionalImageUrls,
            currentStock = row[ProductsTable.currentStock],
            weightKg = row[ProductsTable.weightKg],
            averageRating = row[ProductsTable.averageRating],
            reviewCount = row[ProductsTable.reviewCount],
            category = category
        )
    }

    override suspend fun getAllProducts(): List<ProductResponse> = dbQuery {
        (ProductsTable innerJoin CategoriesTable)
            .selectAll()
            .map(::resultRowToProductResponse)
    }

    override suspend fun getProductById(id: String): ProductResponse? = dbQuery {
        (ProductsTable innerJoin CategoriesTable)
            .selectAll().where { ProductsTable.id eq id }
            .map(::resultRowToProductResponse)
            .singleOrNull()
    }

    override suspend fun addProduct(request: ProductRequest): Product {

        dbQuery {
            val categoryExists = CategoriesTable.selectAll().where { CategoriesTable.id eq request.categoryId }.count() > 0
            if (!categoryExists) {
                throw BadRequestException("Invalid categoryId: The provided category does not exist.")
            }
        }

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
            averageRating = 0.0,
            reviewCount = 0
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

    override suspend fun updateProduct(id: String, request: ProductRequest): Boolean {

        dbQuery {
            val categoryExists = CategoriesTable.selectAll().where { CategoriesTable.id eq request.categoryId }.count() > 0
            if (!categoryExists) {
                throw BadRequestException("Invalid categoryId: The provided category does not exist.")
            }
        }

        return dbQuery {
            ProductsTable.update({ ProductsTable.id eq id }) {
                it[sku] = request.sku
                it[name] = request.name
                it[description] = request.description
                it[price] = request.price
                it[salePrice] = request.salePrice
                it[mainImageUrl] = request.mainImageUrl
                it[additionalImageUrls] = request.additionalImageUrls.joinToString(delimiter)
                it[categoryId] = request.categoryId
                it[currentStock] = request.currentStock
                it[weightKg] = request.weightKg
                it[supplierId] = request.supplierId
                it[costPrice] = request.costPrice
                it[isConsigned] = request.isConsigned
            } > 0
        }
    }

    override suspend fun deleteProduct(id: String): Boolean = dbQuery {
        ProductsTable.deleteWhere { ProductsTable.id eq id } > 0
    }

    override suspend fun getAllProductSummaries(): List<ProductSummaryResponse> = dbQuery {
        (ProductsTable innerJoin CategoriesTable)
            .selectAll()
            .map {
                ProductSummaryResponse(
                    id = it[ProductsTable.id],
                    name = it[ProductsTable.name],
                    price = it[ProductsTable.price],
                    salePrice = it[ProductsTable.salePrice],
                    mainImageUrl = it[ProductsTable.mainImageUrl],
                    averageRating = it[ProductsTable.averageRating],
                    category = Category(
                        id = it[CategoriesTable.id],
                        name = it[CategoriesTable.name],
                        imageUrl = it[CategoriesTable.imageUrl]
                    )
                )
            }
    }
}