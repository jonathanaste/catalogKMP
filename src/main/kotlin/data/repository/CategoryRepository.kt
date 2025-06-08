package com.example.data.repository

import com.example.data.model.Category

interface CategoryRepository {
    suspend fun getAllCategories(): List<Category>
    suspend fun addCategory(category: Category): Category
}