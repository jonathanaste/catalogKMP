package data.model

import kotlinx.serialization.Serializable

@Serializable
data class AnswerRequest(
    val answerText: String
)