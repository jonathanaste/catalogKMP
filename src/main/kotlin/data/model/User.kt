package com.example.data.model

import kotlinx.serialization.Serializable

/**
 * Representa la información de un usuario que es segura para ser expuesta
 * a través de la API. No contiene datos sensibles como el hash de la contraseña.
 * Basado en el Documento Técnico. 
 */
@Serializable
data class User(
    val id: String,
    val email: String,
    val name: String,
    val role: String // Ej. "CLIENTE", "ADMIN"
)