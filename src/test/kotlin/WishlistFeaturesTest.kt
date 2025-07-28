import com.example.data.model.*
import data.model.AddToWishlistRequest
import data.model.WishlistResponse
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

class WishlistFeaturesTest {

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
    fun `Test full wishlist user flow`() = testApplication {
        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }

        // --- PART 1: SETUP - Create a product to be added to the wishlist ---
        val adminToken = getAuthToken("admin@example.com", "adminpass")
        val uniqueSuffix = System.currentTimeMillis()

        // Create a temporary category first
        val categoryRequest = CategoryRequest(
            name = "Wishlist Test Category - $uniqueSuffix",
            imageUrl = "http://example.com/wishlist-cat.png" // ADDED
        )
        val category = client.post("/admin/categories") {
            bearerAuth(adminToken)
            contentType(ContentType.Application.Json)
            setBody(categoryRequest)
        }.body<Category>()

        // Create a unique product for this test run with all fields populated
        val productRequest = ProductRequest(
            sku = "WISHLIST-SKU-$uniqueSuffix",
            name = "Product for Wishlist",
            description = "A dynamic product for testing the wishlist feature.",
            price = 49.99,
            salePrice = 39.99, // ADDED
            mainImageUrl = "http://example.com/wishlist-product.png",
            additionalImageUrls = listOf("http://example.com/wishlist-extra.png"), // ADDED
            categoryId = category.id,
            currentStock = 50,
            weightKg = 0.2, // ADDED
            supplierId = null, // ADDED
            costPrice = 20.0,
            isConsigned = false
        )
        val product = client.post("/admin/products") {
            bearerAuth(adminToken)
            contentType(ContentType.Application.Json)
            setBody(productRequest)
        }.body<Product>()

        // --- PART 2: CLIENT LIFECYCLE ---
        // Register and log in a new client user
        val userEmail = "wishlist-user-${System.currentTimeMillis()}@example.com"
        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest(userEmail, "password123", "Wishlist", "User"))
        }
        val userToken = getAuthToken(userEmail, "password123")


        // --- PART 3: TEST WISHLIST FUNCTIONALITY ---

        // 1. Initially, the wishlist should be empty
        val initialWishlist = client.get("/wishlist") {
            bearerAuth(userToken)
        }.body<List<WishlistResponse>>()
        assertTrue(initialWishlist.isEmpty(), "Wishlist should be empty initially.")

        // 2. Add the product to the wishlist
        val addToWishlistRequest = AddToWishlistRequest(productId = product.id)
        val addResponse = client.post("/wishlist/add") {
            bearerAuth(userToken)
            contentType(ContentType.Application.Json)
            setBody(addToWishlistRequest)
        }
        assertEquals(HttpStatusCode.Created, addResponse.status, "Failed to add item to wishlist.")

        // 3. Verify the wishlist now contains the product
        val updatedWishlist = client.get("/wishlist") {
            bearerAuth(userToken)
        }.body<List<WishlistResponse>>()
        assertEquals(1, updatedWishlist.size, "Wishlist should contain one item.")
        assertEquals(product.id, updatedWishlist.first().product.id, "Wishlist contains the wrong product.")

        // 4. Attempt to add the same product again (should not create a duplicate)
        val addAgainResponse = client.post("/wishlist/add") {
            bearerAuth(userToken)
            contentType(ContentType.Application.Json)
            setBody(addToWishlistRequest)
        }
        assertEquals(HttpStatusCode.OK, addAgainResponse.status, "Adding a duplicate item should return OK.")

        // 5. Remove the product from the wishlist
        val removeResponse = client.post("/wishlist/remove") {
            bearerAuth(userToken)
            contentType(ContentType.Application.Json)
            setBody(addToWishlistRequest)
        }
        assertEquals(HttpStatusCode.NoContent, removeResponse.status, "Failed to remove item from wishlist.")

        // 6. Verify the wishlist is empty again
        val finalWishlist = client.get("/wishlist") {
            bearerAuth(userToken)
        }.body<List<WishlistResponse>>()
        assertTrue(finalWishlist.isEmpty(), "Wishlist should be empty after removing the item.")
    }
}