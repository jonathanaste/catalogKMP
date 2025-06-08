package com.example.data.repository

import com.example.data.model.Product
import com.example.data.model.ProductResponse

interface ProductRepository {
    suspend fun getAllProducts(): List<ProductResponse>
    suspend fun getProductById(id: String): ProductResponse?
    suspend fun addProduct(product: Product): Product
    suspend fun updateProduct(id: String, product: Product): Boolean
    suspend fun deleteProduct(id: String): Boolean
}