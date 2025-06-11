package com.example.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: String,
    val name: String,
    val imageUrl: String?
)

@Serializable
data class CategoryRequest(
    val name: String,
    val imageUrl: String?
)