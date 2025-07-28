package data.repository

import com.example.data.model.*
import com.example.plugins.DatabaseFactory.dbQuery
import data.model.AnswerRequest
import data.model.ProductQuestion
import data.model.QuestionAnswer
import data.model.QuestionRequest
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.selectAll
import java.util.*

class QuestionRepositoryImpl : QuestionRepository {

    private fun resultRowToQuestion(row: ResultRow): ProductQuestion {
        val answerText = row.getOrNull(QuestionAnswersTable.answerText)
        val answerDate = row.getOrNull(QuestionAnswersTable.date)
        val answer = if (answerText != null && answerDate != null) {
            QuestionAnswer(answerText, answerDate)
        } else {
            null
        }

        return ProductQuestion(
            id = row[ProductQuestionsTable.id],
            productId = row[ProductQuestionsTable.productId],
            userId = row[ProductQuestionsTable.userId],
            userName = row[ProductQuestionsTable.userName],
            questionText = row[ProductQuestionsTable.questionText],
            date = row[ProductQuestionsTable.date],
            answer = answer
        )
    }

    override suspend fun getQuestionsForProduct(productId: String): List<ProductQuestion> = dbQuery {
        ProductQuestionsTable
            .leftJoin(QuestionAnswersTable, { ProductQuestionsTable.id }, { QuestionAnswersTable.questionId })
            .selectAll().where { ProductQuestionsTable.productId eq productId }
            .orderBy(ProductQuestionsTable.date, SortOrder.DESC)
            .map(::resultRowToQuestion)
    }

    override suspend fun addQuestion(productId: String, userId: String, userName: String, request: QuestionRequest): ProductQuestion {
        val newId = UUID.randomUUID().toString()
        val currentTime = System.currentTimeMillis()
        return dbQuery {
            ProductQuestionsTable.insert {
                it[id] = newId
                it[this.productId] = productId
                it[this.userId] = userId
                it[this.userName] = userName
                it[questionText] = request.questionText
                it[date] = currentTime
            }
            ProductQuestion(newId, productId, userId, userName, request.questionText, currentTime, null)
        }
    }

    override suspend fun addAnswerToQuestion(questionId: String, request: AnswerRequest): ProductQuestion? {
        val currentTime = System.currentTimeMillis()

        // We need the original question data to build the final response object.
        val originalQuestion = dbQuery {
            ProductQuestionsTable
                .selectAll()
                .where { ProductQuestionsTable.id eq questionId }
                .singleOrNull()
                ?.let {
                    // Manually map to our data class
                    ProductQuestion(
                        id = it[ProductQuestionsTable.id],
                        productId = it[ProductQuestionsTable.productId],
                        userId = it[ProductQuestionsTable.userId],
                        userName = it[ProductQuestionsTable.userName],
                        questionText = it[ProductQuestionsTable.questionText],
                        date = it[ProductQuestionsTable.date],
                        answer = null // Answer is not yet present
                    )
                }
        } ?: return null // Return null if the question doesn't exist

        // Now, perform the insert in a separate transaction.
        dbQuery {
            QuestionAnswersTable.insert {
                it[this.questionId] = questionId
                it[answerText] = request.answerText
                it[date] = currentTime
            }
        }

        // Return the original question, but with the new answer manually added.
        // This is efficient and avoids transaction visibility issues.
        return originalQuestion.copy(
            answer = QuestionAnswer(
                answerText = request.answerText,
                date = currentTime
            )
        )
    }
}