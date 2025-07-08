package data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfileUpdateRequest(
    val firstName: String,
    val lastName: String,
    val phone: String?
)