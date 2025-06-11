package com.example.data.repository

import com.example.data.model.CategoriesTable
import com.example.data.model.Category
import com.example.data.model.Product
import com.example.data.model.ProductResponse
import com.example.data.model.ProductsTable
import com.example.plugins.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class ProductRepositoryImpl : ProductRepository {


    // Función auxiliar para convertir una fila de la DB a nuestro objeto Product
    private fun resultRowToProduct(row: ResultRow) = Product(
        id = row[ProductsTable.id],
        name = row[ProductsTable.name],
        description = row[ProductsTable.description],
        price = row[ProductsTable.price],
        mainImageUrl = row[ProductsTable.mainImageUrl],
        categoryId = row[ProductsTable.categoryId],
        stockQuantity = row[ProductsTable.stockQuantity],
        supplierId = row[ProductsTable.supplierId],
        costPrice = row[ProductsTable.costPrice],
        isConsigned = row[ProductsTable.isConsigned],
    )

    // Función auxiliar para convertir una fila de la DB a nuestro objeto ProductResponse
    private fun resultRowToProductResponse(row: ResultRow) = ProductResponse(
        id = row[ProductsTable.id],
        name = row[ProductsTable.name],
        description = row[ProductsTable.description],
        price = row[ProductsTable.price],
        mainImageUrl = row[ProductsTable.mainImageUrl],
        stockQuantity = row[ProductsTable.stockQuantity],
        supplierId = row[ProductsTable.supplierId],
        costPrice = row[ProductsTable.costPrice],
        isConsigned = row[ProductsTable.isConsigned],
        category = Category( // Creamos el objeto Category desde la misma fila
            id = row[CategoriesTable.id],
            name = row[CategoriesTable.name],
            imageUrl = row[CategoriesTable.imageUrl]
        )
    )

    override suspend fun getAllProducts(): List<ProductResponse> = dbQuery {
        ProductsTable
            .innerJoin(CategoriesTable) // <-- LA CLAVE: Unimos las tablas
            .selectAll()
            .map(::resultRowToProductResponse)
    }

    override suspend fun getProductById(id: String): ProductResponse? = dbQuery {
        ProductsTable
            .innerJoin(CategoriesTable) // <-- LA CLAVE: Unimos las tablas
            .selectAll().where { ProductsTable.id eq id }
            .map(::resultRowToProductResponse)
            .singleOrNull()
    }

    override suspend fun addProduct(product: Product): Product = dbQuery {
        val insertStatement = ProductsTable.insert {
            it[id] = product.id
            it[name] = product.name
            it[description] = product.description
            it[price] = product.price
            it[mainImageUrl] = product.mainImageUrl
            it[categoryId] = product.categoryId
            it[stockQuantity] = product.stockQuantity
            it[supplierId] = product.supplierId
            it[costPrice] = product.costPrice
            it[isConsigned] = product.isConsigned
        }
        resultRowToProduct(insertStatement.resultedValues!!.first())
    }

    override suspend fun updateProduct(id: String, product: Product): Boolean = dbQuery {
        ProductsTable.update({ ProductsTable.id eq id }) {
            it[name] = product.name
            it[description] = product.description
            it[price] = product.price
            it[mainImageUrl] = product.mainImageUrl
            it[categoryId] = product.categoryId
            it[stockQuantity] = product.stockQuantity
            it[supplierId] = product.supplierId
            it[costPrice] = product.costPrice
            it[isConsigned] = product.isConsigned
        } > 0
    }

    override suspend fun deleteProduct(id: String): Boolean = dbQuery {
        ProductsTable.deleteWhere { ProductsTable.id eq id } > 0
    }
}