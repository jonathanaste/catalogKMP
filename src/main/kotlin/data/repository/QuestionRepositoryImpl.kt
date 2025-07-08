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
        return dbQuery {
            // Check if the question exists first
            val question = ProductQuestionsTable.selectAll().where { ProductQuestionsTable.id eq questionId }.singleOrNull()
            if (question == null) {
                return@dbQuery null
            }

            // Insert the answer
            QuestionAnswersTable.insert {
                it[this.questionId] = questionId
                it[answerText] = request.answerText
                it[date] = currentTime
            }

            // Return the full question with the new answer
            getQuestionsForProduct(question[ProductQuestionsTable.productId])
                .find { it.id == questionId }
        }
    }
}