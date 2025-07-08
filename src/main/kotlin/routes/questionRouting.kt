package routes

import com.example.plugins.BadRequestException
import com.example.plugins.ForbiddenException
import com.example.plugins.NotFoundException
import data.model.AnswerRequest
import data.model.QuestionRequest
import data.repository.QuestionRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.questionRouting() {
    val repository: QuestionRepository by inject()

    // Public endpoint to get questions
    get("/products/{productId}/questions") {
        val productId = call.parameters["productId"] ?: throw BadRequestException("Product ID is missing.")
        val questions = repository.getQuestionsForProduct(productId)
        call.respond(questions)
    }

    authenticate("auth-jwt") {
        // Protected endpoint for users to post questions
        post("/products/{productId}/questions") {
            val principal = call.principal<JWTPrincipal>()!!
            val userId = principal.getClaim("userId", String::class)!!
            val userName = "${principal.getClaim("firstName", String::class)} ${principal.getClaim("lastName", String::class).orEmpty()}".trim()
            
            val productId = call.parameters["productId"] ?: throw BadRequestException("Product ID is missing.")
            val request = call.receive<QuestionRequest>()

            val newQuestion = repository.addQuestion(productId, userId, userName, request)
            call.respond(HttpStatusCode.Created, newQuestion)
        }

        // Protected endpoint for admins to post answers
        post("/admin/questions/{questionId}/answer") {
            val principal = call.principal<JWTPrincipal>()!!
            if (principal.getClaim("role", String::class) != "ADMIN") {
                throw ForbiddenException("Administrator role required.")
            }

            val questionId = call.parameters["questionId"] ?: throw BadRequestException("Question ID is missing.")
            val request = call.receive<AnswerRequest>()

            val updatedQuestion = repository.addAnswerToQuestion(questionId, request)
                ?: throw NotFoundException("Question with id $questionId not found.")
            
            call.respond(HttpStatusCode.OK, updatedQuestion)
        }
    }
}