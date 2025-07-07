package data.model

import kotlinx.serialization.Serializable

@Serializable
data class Address(
    val id: String,
    val alias: String, // "Home", "Office"
    val street: String,
    val number: String,
    val postalCode: String,
    val city: String,
    val state: String,
    val isDefault: Boolean = false // <-- Added for convenience
)