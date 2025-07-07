package data.model

import kotlinx.serialization.Serializable

@Serializable
data class AddressRequest(
    val alias: String,
    val street: String,
    val number: String,
    val postalCode: String,
    val city: String,
    val state: String,
    val isDefault: Boolean = false
)