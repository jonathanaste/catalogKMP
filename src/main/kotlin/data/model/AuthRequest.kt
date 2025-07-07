package com.example.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val firstName: String, // <-- UPDATED
    val lastName: String   // <-- UPDATED
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)