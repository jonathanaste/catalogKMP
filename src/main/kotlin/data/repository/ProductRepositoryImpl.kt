package com.example.data.repository

import com.example.data.model.*
import com.example.plugins.DatabaseFactory.dbQuery
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.UUID

class ProductRepositoryImpl : ProductRepository {

    // JSON helper for attributes
    private fun serializeAttributes(attributes: Map<String, String>?): String? {
        return attributes?.let { Json.encodeToString(it) }
    }

    private fun deserializeAttributes(jsonString: String?): Map<String, String>? {
        return jsonString?.let { Json.decodeFromString<Map<String, String>>(it) }
    }

    private fun resultRowToProductVariant(row: ResultRow) = ProductVariant(
        id = row[ProductVariantsTable.id],
        productId = row[ProductVariantsTable.productId],
        sku = row[ProductVariantsTable.sku],
        name = row[ProductVariantsTable.name],
        price = row[ProductVariantsTable.price],
        stockQuantity = row[ProductVariantsTable.stockQuantity],
        attributes = deserializeAttributes(row[ProductVariantsTable.attributes]),
        imageUrl = row[ProductVariantsTable.imageUrl]
        // createdAt and updatedAt are handled by DB
    )

    // Updated to include hasVariants and potentially variants (though variants are loaded separately)
    private fun resultRowToProduct(row: ResultRow, variants: List<ProductVariant>? = null) = Product(
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
        hasVariants = row[ProductsTable.hasVariants],
        variants = variants
        // createdAt and updatedAt could be added if needed from DB
    )

    // Updated to include hasVariants and variants
    private fun resultRowToProductResponse(row: ResultRow, variants: List<ProductVariant>? = null) = ProductResponse(
        id = row[ProductsTable.id],
        name = row[ProductsTable.name],
        description = row[ProductsTable.description],
        price = row[ProductsTable.price],
        mainImageUrl = row[ProductsTable.mainImageUrl],
        stockQuantity = row[ProductsTable.stockQuantity],
        supplierId = row[ProductsTable.supplierId],
        costPrice = row[ProductsTable.costPrice],
        isConsigned = row[ProductsTable.isConsigned],
        hasVariants = row[ProductsTable.hasVariants], // Add hasVariants to ProductResponse
        category = Category(
            id = row[CategoriesTable.id],
            name = row[CategoriesTable.name],
            imageUrl = row[CategoriesTable.imageUrl]
        ),
        variants = variants // Add variants to ProductResponse
    )


    override suspend fun getAllProducts(): List<ProductResponse> = dbQuery {
        // 1. Fetch all base products with their categories
        val productRows = ProductsTable
            .innerJoin(CategoriesTable)
            .selectAll()
            .toList()

        if (productRows.isEmpty()) return@dbQuery emptyList()

        // 2. Collect all productIds for products that have variants
        val productIdsWithVariants = productRows
            .filter { it[ProductsTable.hasVariants] }
            .map { it[ProductsTable.id] }
            .toSet()

        // 3. Fetch all variants for these product IDs in a single query
        val allVariantsForProducts: Map<String, List<ProductVariant>> = if (productIdsWithVariants.isNotEmpty()) {
            ProductVariantsTable
                .selectAll().where { ProductVariantsTable.productId inList productIdsWithVariants }
                .map(::resultRowToProductVariant)
                .groupBy { it.productId }
        } else {
            emptyMap()
        }

        // 4. Map base products to ProductResponse, embedding the corresponding variants
        productRows.map { baseProductRow ->
            val productId = baseProductRow[ProductsTable.id]
            val variantsForThisProduct = allVariantsForProducts[productId]
            resultRowToProductResponse(baseProductRow, variantsForThisProduct)
        }
    }

    override suspend fun getProductById(id: String): ProductResponse? = dbQuery {
        val productRow = ProductsTable
            .innerJoin(CategoriesTable)
            .selectAll().where { ProductsTable.id eq id }
            .singleOrNull()

        productRow?.let { baseProductRow ->
            val productId = baseProductRow[ProductsTable.id]
            val hasVariants = baseProductRow[ProductsTable.hasVariants]
            var variants: List<ProductVariant>? = null

            if (hasVariants) {
                variants = ProductVariantsTable
                    .selectAll().where { ProductVariantsTable.productId eq productId }
                    .map(::resultRowToProductVariant)
            }
            resultRowToProductResponse(baseProductRow, variants)
        }
    }

    override suspend fun addProduct(product: Product): Product = dbQuery {
        // Insert base product
        val insertedProductRow = ProductsTable.insert {
            it[id] = product.id
            it[name] = product.name
            it[description] = product.description
            it[price] = product.price
            it[mainImageUrl] = product.mainImageUrl
            it[categoryId] = product.categoryId
            it[stockQuantity] = product.stockQuantity // Base stock
            it[supplierId] = product.supplierId
            it[costPrice] = product.costPrice
            it[isConsigned] = product.isConsigned
            it[hasVariants] = product.variants?.isNotEmpty() ?: false
        }.resultedValues!!.first()

        val createdVariants = mutableListOf<ProductVariant>()

        // Insert variants if any
        product.variants?.forEach { variantRequest ->
            val variantId = variantRequest.id // Assuming ID is passed if it's a conceptual variant object
            ProductVariantsTable.insert {
                it[id] = variantId
                it[productId] = product.id
                it[sku] = variantRequest.sku
                it[name] = variantRequest.name
                it[price] = variantRequest.price
                it[stockQuantity] = variantRequest.stockQuantity
                it[attributes] = serializeAttributes(variantRequest.attributes)
                it[imageUrl] = variantRequest.imageUrl
            }
            // For returning the created variant, we'd ideally fetch it or construct it carefully
            // For now, let's assume variantRequest contains enough info or we simplify the return
            createdVariants.add(variantRequest) // Simplified, ideally map from insert result or re-fetch
        }
        resultRowToProduct(insertedProductRow, createdVariants.ifEmpty { null })
    }


    override suspend fun updateProduct(id: String, product: Product): Boolean = dbQuery {
        // 1. Update base product details
        val updatedRows = ProductsTable.update({ ProductsTable.id eq id }) {
            it[name] = product.name
            it[description] = product.description
            it[price] = product.price // Base product price
            it[mainImageUrl] = product.mainImageUrl
            it[categoryId] = product.categoryId
            it[stockQuantity] = product.stockQuantity // Base product stock
            it[supplierId] = product.supplierId
            it[costPrice] = product.costPrice
            it[isConsigned] = product.isConsigned
            it[hasVariants] = product.variants?.isNotEmpty() ?: false
        }

        if (updatedRows == 0) return@dbQuery false // Product not found

        // 2. Manage variants
        if (product.variants != null) {
            val existingVariantIds = ProductVariantsTable
                .slice(ProductVariantsTable.id)
                .selectAll().where { ProductVariantsTable.productId eq id }
                .map { it[ProductVariantsTable.id] }
                .toSet()

            val incomingVariantIds = product.variants.mapNotNull { it.id }.toSet()

            // Delete variants not in incoming list
            val variantsToDelete = existingVariantIds - incomingVariantIds
            if (variantsToDelete.isNotEmpty()) {
                ProductVariantsTable.deleteWhere { (ProductVariantsTable.productId eq id) and (ProductVariantsTable.id inList variantsToDelete) }
            }

            // Update existing or insert new variants
            product.variants.forEach { variant ->
                if (variant.id in existingVariantIds) { // Update existing variant
                    ProductVariantsTable.update({ (ProductVariantsTable.productId eq id) and (ProductVariantsTable.id eq variant.id) }) {
                        it[sku] = variant.sku
                        it[name] = variant.name
                        it[price] = variant.price
                        it[stockQuantity] = variant.stockQuantity
                        it[attributes] = serializeAttributes(variant.attributes)
                        it[imageUrl] = variant.imageUrl
                    }
                } else { // Insert new variant
                    ProductVariantsTable.insert {
                        it[ProductVariantsTable.id] = variant.id // Assumes new variants also come with a pre-generated ID
                        it[productId] = id
                        it[sku] = variant.sku
                        it[name] = variant.name
                        it[price] = variant.price
                        it[stockQuantity] = variant.stockQuantity
                        it[attributes] = serializeAttributes(variant.attributes)
                        it[imageUrl] = variant.imageUrl
                    }
                }
            }
        } else {
            // If product.variants is null, it implies all variants should be removed
            // (or handle based on product.hasVariants flag if it was explicitly set to false)
            ProductVariantsTable.deleteWhere { ProductVariantsTable.productId eq id }
        }
        true // Successfully updated
    }


    override suspend fun deleteProduct(id: String): Boolean = dbQuery {
        // Variants will be deleted by CASCADE constraint in DB
        ProductsTable.deleteWhere { ProductsTable.id eq id } > 0
    }
}