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

class Phase2FeaturesTest {

    private fun extractToken(loginBody: String): String {
        return Json.parseToJsonElement(loginBody).jsonObject["token"]!!.jsonPrimitive.content
    }

    // A helper function to create a user and get a token.
    private suspend fun ApplicationTestBuilder.getAuthToken(
        email: String,
        password: String
    ): String {
        val client = createClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val loginResponse = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, password))
        }

        assertEquals(HttpStatusCode.OK, loginResponse.status, "Login failed for user: $email. Please ensure the user exists and the password is correct.")
        return extractToken(loginResponse.body<String>())
    }


    @Test
    fun `Test full user flow for Phase 2 features`() = testApplication {
        application {
            // The testApplication environment will automatically load your main module.
        }

        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }

        // --- PART 0: ADMIN SETUP & SEEDING DATA ---
        // **CORRECTED CREDENTIALS**
        // Using the correct admin credentials from your Catalog.http setup file.
        val adminToken = getAuthToken("admin@example.com", "adminpass")

        // Create a unique category for this test run
        val categoryRequest = CategoryRequest(name = "Test Category - ${System.currentTimeMillis()}", imageUrl = "http://example.com/cat.png")
        val categoryResponse = client.post("/admin/categories") {
            bearerAuth(adminToken)
            contentType(ContentType.Application.Json)
            setBody(categoryRequest)
        }
        assertEquals(HttpStatusCode.Created, categoryResponse.status, "Admin setup failed: Could not create category.")
        val category = categoryResponse.body<Category>()

        // Create a unique product for this test run
        val productRequest = ProductRequest(
            sku = "TEST-SKU-${System.currentTimeMillis()}",
            name = "Testable Super Serum",
            description = "A product created dynamically for testing.",
            price = 99.99,
            mainImageUrl = "http://example.com/product.png",
            categoryId = category.id,
            currentStock = 100,
            supplierId = null,
            costPrice = 50.0,
            isConsigned = false
        )
        val productResponse = client.post("/admin/products") {
            bearerAuth(adminToken)
            contentType(ContentType.Application.Json)
            setBody(productRequest)
        }
        assertEquals(HttpStatusCode.Created, productResponse.status, "Admin setup failed: Could not create product.")
        val product = productResponse.body<Product>()

        // --- PART 1: CLIENT USER REGISTRATION AND LOGIN ---
        val uniqueEmail = "client.${System.currentTimeMillis()}@example.com"
        val registerRequest = RegisterRequest(uniqueEmail, "password123", "Client", "User")

        val registerResponse = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(registerRequest)
        }
        assertEquals(HttpStatusCode.Created, registerResponse.status, "Client registration failed.")

        val clientToken = getAuthToken(uniqueEmail, "password123")

        // --- PART 2: ACCOUNT MANAGEMENT (ADDRESS) ---
        val addressRequest = AddressRequest("Home", "123 Test St", "Apt 4B", "10001", "Testville", "Testland")
        val addAddressResponse = client.post("/users/me/addresses") {
            bearerAuth(clientToken)
            contentType(ContentType.Application.Json)
            setBody(addressRequest)
        }
        assertEquals(HttpStatusCode.Created, addAddressResponse.status)
        val createdAddress = addAddressResponse.body<Address>()

        // --- PART 3: SOCIAL FEATURES (REVIEW) ---
        val reviewRequest = ReviewRequest(rating = 5, title = "Great Product!", comment = "I really enjoyed this.")
        client.post("/products/${product.id}/reviews") {
            bearerAuth(clientToken)
            contentType(ContentType.Application.Json)
            setBody(reviewRequest)
        }

        // --- PART 4: CHECKOUT FLOW ---
        val cartItem = CartItem(productId = product.id, quantity = 2)
        client.post("/cart/add") {
            bearerAuth(clientToken)
            contentType(ContentType.Application.Json)
            setBody(cartItem)
        }

        val checkoutRequest = CheckoutRequest(addressId = createdAddress.id)
        client.post("/orders/checkout") {
            bearerAuth(clientToken)
            contentType(ContentType.Application.Json)
            setBody(checkoutRequest)
        }
    }
}