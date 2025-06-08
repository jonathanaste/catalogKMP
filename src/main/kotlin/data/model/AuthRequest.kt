package com.example.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String
    // puedes añadir apellido, teléfono, etc. si lo deseas
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)