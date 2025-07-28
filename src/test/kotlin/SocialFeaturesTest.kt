import com.example.data.model.*
import data.model.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Test
import kotlin.test.*

class SocialFeaturesTest {

    private fun extractToken(loginBody: String): String {
        return Json.parseToJsonElement(loginBody).jsonObject["token"]!!.jsonPrimitive.content
    }

    private suspend fun ApplicationTestBuilder.getAuthToken(email: String, password: String): String {
        val client = createClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val loginResponse = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, password))
        }
        assertEquals(HttpStatusCode.OK, loginResponse.status, "Login failed for user: $email.")
        return extractToken(loginResponse.body<String>())
    }

    @Test
    fun `Test full social features user flow for reviews and questions`() = testApplication {
        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }

        // --- PART 1: SETUP - Create a product for social interactions ---
        val adminToken = getAuthToken("admin@example.com", "adminpass")

        val category = client.post("/admin/categories") {
            bearerAuth(adminToken)
            contentType(ContentType.Application.Json)
            setBody(CategoryRequest(
                name = "Social Test Category - ${System.currentTimeMillis()}",
                imageUrl = "TODO()"
            ))
        }.body<Category>()

        // Create a unique product with all fields populated
        val productRequest = ProductRequest(
            sku = "SOCIAL-SKU-${System.currentTimeMillis()}",
            name = "Product for Reviews",
            description = "A product for testing social features.",
            price = 150.0,
            salePrice = 125.0, // <-- ADDED
            categoryId = category.id,
            currentStock = 100,
            mainImageUrl = "http://example.com/social-product.png",
            additionalImageUrls = listOf("http://example.com/social-extra.png"), // <-- ADDED
            weightKg = 0.3, // <-- ADDED
            supplierId = null, // <-- ADDED
            costPrice = 75.0,
            isConsigned = false
        )
        val product = client.post("/admin/products") {
            bearerAuth(adminToken)
            contentType(ContentType.Application.Json)
            setBody(productRequest)
        }.body<Product>()

        // --- PART 2: CLIENT LIFECYCLE ---
        val userEmail = "social-user-${System.currentTimeMillis()}@example.com"
        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest(userEmail, "password123", "Social", "User"))
        }
        val userToken = getAuthToken(userEmail, "password123")


        // --- PART 3: TEST REVIEW FUNCTIONALITY ---
        // 1. Post a new review
        val reviewRequest = ReviewRequest(rating = 5, title = "Absolutely Amazing!", comment = "This product exceeded all my expectations.")
        val postReviewResponse = client.post("/products/${product.id}/reviews") {
            bearerAuth(userToken)
            contentType(ContentType.Application.Json)
            setBody(reviewRequest)
        }
        assertEquals(HttpStatusCode.Created, postReviewResponse.status, "Failed to post a review.")

        // 2. Verify the review can be retrieved
        val getReviewsResponse = client.get("/products/${product.id}/reviews")
        assertEquals(HttpStatusCode.OK, getReviewsResponse.status)
        val reviews = getReviewsResponse.body<List<ProductReview>>()
        assertEquals(1, reviews.size, "There should be one review for the product.")
        assertEquals("Absolutely Amazing!", reviews.first().title)


        // --- PART 4: TEST Q&A FUNCTIONALITY ---
        // 1. Post a new question
        val questionRequest = QuestionRequest(questionText = "What are the main ingredients?")
        val postQuestionResponse = client.post("/products/${product.id}/questions") {
            bearerAuth(userToken)
            contentType(ContentType.Application.Json)
            setBody(questionRequest)
        }
        assertEquals(HttpStatusCode.Created, postQuestionResponse.status, "Failed to post a question.")
        val postedQuestion = postQuestionResponse.body<ProductQuestion>()
        assertNull(postedQuestion.answer, "A new question should not have an answer.")
        assertEquals("What are the main ingredients?", postedQuestion.questionText)

        // 2. Admin answers the question
        val answerRequest = AnswerRequest(answerText = "The main ingredients are all-natural and sustainably sourced.")
        val postAnswerResponse = client.post("/admin/questions/${postedQuestion.id}/answer") {
            bearerAuth(adminToken)
            contentType(ContentType.Application.Json)
            setBody(answerRequest)
        }
        assertEquals(HttpStatusCode.OK, postAnswerResponse.status, "Admin failed to post an answer.")
        val answeredQuestion = postAnswerResponse.body<ProductQuestion>()
        assertNotNull(answeredQuestion.answer, "The question should now have an answer.")
        assertEquals("The main ingredients are all-natural and sustainably sourced.", answeredQuestion.answer.answerText)

        // 3. Verify the question with its answer can be retrieved
        val getQuestionsResponse = client.get("/products/${product.id}/questions")
        assertEquals(HttpStatusCode.OK, getQuestionsResponse.status)
        val questions = getQuestionsResponse.body<List<ProductQuestion>>()
        assertEquals(1, questions.size)
        assertNotNull(questions.first().answer, "The retrieved question should include the admin's answer.")
        assertEquals(answerRequest.answerText, questions.first().answer?.answerText)
    }
}