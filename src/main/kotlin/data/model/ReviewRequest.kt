package data.model

import kotlinx.serialization.Serializable

@Serializable
data class ReviewRequest(
    val rating: Int,
    val title: String,
    val comment: String,
    val photoUrls: List<String> = emptyList()
)