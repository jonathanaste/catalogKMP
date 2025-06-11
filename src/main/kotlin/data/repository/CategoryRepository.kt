package com.example.data.repository

import com.example.data.model.Category
import com.example.data.model.CategoryRequest

interface CategoryRepository {
    suspend fun getAllCategories(): List<Category>
    suspend fun getCategoryById(id: String): Category? // <-- AÑADIR
    suspend fun addCategory(category: CategoryRequest): Category
    suspend fun updateCategory(id: String, category: Category): Boolean // <-- AÑADIR
    suspend fun deleteCategory(id: String): Boolean // <-- AÑADIR
}