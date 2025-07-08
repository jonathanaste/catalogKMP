package data.model

import kotlinx.serialization.Serializable

/**
 * Represents an answer provided by an administrator to a product question.
 * Based on the v2.0 Technical Document.
 */
@Serializable
data class QuestionAnswer(
    val answerText: String,
    val date: Long // UNIX Timestamp
)

/**
 * Represents a question asked by a user about a specific product,
 * and may contain an answer.
 * Based on the v2.0 Technical Document.
 */
@Serializable
data class ProductQuestion(
    val id: String,
    val productId: String,
    val userId: String,
    val userName: String, // A snapshot of the user's name
    val questionText: String,
    val date: Long, // UNIX Timestamp
    val answer: QuestionAnswer? = null // The answer is optional
)