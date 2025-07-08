package data.model

import kotlinx.serialization.Serializable

@Serializable
data class QuestionRequest(
    val questionText: String
)