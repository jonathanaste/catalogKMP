package com.example.data.model

import data.model.Address
import kotlinx.serialization.Serializable

/**
 * Represents user information that is safe to be exposed via the API.
 * It does not contain sensitive data like the password hash.
 * Based on the v2.0 Technical Document.
 */
@Serializable
data class User(
    val id: String,
    val email: String,
    val firstName: String, // <-- NEW
    val lastName: String,  // <-- NEW
    val phone: String?,    // <-- NEW
    val role: String,      // e.g., "CLIENT", "ADMIN", "RESELLER"
    val addresses: List<Address> = emptyList() // <-- NEW
)