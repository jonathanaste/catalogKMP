package data.repository

import data.model.AnswerRequest
import data.model.ProductQuestion
import data.model.QuestionRequest

interface QuestionRepository {
    suspend fun getQuestionsForProduct(productId: String): List<ProductQuestion>
    suspend fun addQuestion(productId: String, userId: String, userName: String, request: QuestionRequest): ProductQuestion
    suspend fun addAnswerToQuestion(questionId: String, request: AnswerRequest): ProductQuestion?
}