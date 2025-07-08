package data.model

import kotlinx.serialization.Serializable

@Serializable
data class ProductReview(
    val id: String,
    val productId: String,
    val userId: String,
    val userName: String, // A snapshot of the user's name
    val rating: Int,      // 1 to 5
    val title: String,
    val comment: String,
    val photoUrls: List<String> = emptyList(),
    val date: Long // UNIX Timestamp
)