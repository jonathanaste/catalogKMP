package com.example.data.repository

import com.example.data.model.Product
import com.example.data.model.ProductRequest
import com.example.data.model.ProductResponse
import data.model.ProductSummaryResponse

/**
 * Interface for data operations related to products.
 * Aligned with the v2.0 specification.
 */
interface ProductRepository {
    /**
     * Retrieves all products from the database.
     * @return A list of all products.
     */
    suspend fun getAllProducts(): List<ProductResponse>

    /**
     * Finds a single product by its unique ID.
     * @param id The UUID of the product to find.
     * @return The found [Product] or null if not found.
     */
    suspend fun getProductById(id: String): ProductResponse?

    /**
     * Adds a new product to the database.
     * @param request The [ProductRequest] DTO containing the new product's data.
     * @return The newly created [Product] with its generated ID and default values.
     */
    suspend fun addProduct(request: ProductRequest): Product

    /**
     * Updates an existing product in the database.
     * @param id The ID of the product to update.
     * @param request The [ProductRequest] DTO with the updated data.
     * @return True if the update was successful, false otherwise.
     */
    suspend fun updateProduct(id: String, request: ProductRequest): Boolean

    /**
     * Deletes a product from the database.
     * @param id The ID of the product to delete.
     * @return True if the deletion was successful, false otherwise.
     */
    suspend fun deleteProduct(id: String): Boolean

    suspend fun getAllProductSummaries(): List<ProductSummaryResponse>

}