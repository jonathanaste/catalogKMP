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

class ResellerFlowsTest {

    private fun extractToken(loginBody: String): String {
        return Json.parseToJsonElement(loginBody).jsonObject["token"]!!.jsonPrimitive.content
    }

    private suspend fun ApplicationTestBuilder.getAuthToken(email: String, password: String): String {
        val client = createClient {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
        val loginResponse = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, password))
        }
        assertEquals(HttpStatusCode.OK, loginResponse.status, "Login failed for user: $email.")
        return extractToken(loginResponse.body<String>())
    }

    @Test
    fun `Test full reseller lifecycle from creation to attributed sale`() = testApplication {
        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }

        // --- PART 1: ADMIN CREATES A NEW RESELLER ---
        val adminToken = getAuthToken("admin@example.com", "adminpass")
        val uniqueSuffix = System.currentTimeMillis()
        val resellerEmail = "reseller-$uniqueSuffix@example.com"
        val resellerSlug = "reseller-store-$uniqueSuffix"

        val createRequest = ResellerCreateRequest(
            email = resellerEmail,
            firstName = "Test",
            lastName = "Reseller",
            phone = "123456789",
            uniqueStoreSlug = resellerSlug,
            commissionRate = 25.0
        )

        val createResponse = client.post("/admin/resellers") {
            bearerAuth(adminToken)
            contentType(ContentType.Application.Json)
            setBody(createRequest)
        }
        assertEquals(HttpStatusCode.Created, createResponse.status, "Admin failed to create reseller.")
        val resellerUser = createResponse.body<User>()


        // --- PART 2: A CUSTOMER MAKES A PURCHASE VIA RESELLER LINK ---
        val customerEmail = "customer-$uniqueSuffix@example.com"
        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest(customerEmail, "password123", "Loyal", "Customer"))
        }
        val customerToken = getAuthToken(customerEmail, "password123")

        val categoryResponse = client.post("/admin/categories") {
            bearerAuth(adminToken)
            contentType(ContentType.Application.Json)
            setBody(CategoryRequest("Reseller Test Category - $uniqueSuffix", null))
        }
        assertEquals(HttpStatusCode.Created, categoryResponse.status, "Failed to create category.")
        val category = categoryResponse.body<Category>()


        val supplier = client.post("/admin/suppliers") {
            bearerAuth(adminToken)
            contentType(ContentType.Application.Json)
            setBody(CreateSupplierRequest(name = "Test Supplier"))
        }.body<Supplier>()

        // *** FIX: Use named arguments for ProductRequest for clarity and correctness ***
        val productRequest = ProductRequest(
            sku = "R-SKU-001",
            name = "Reseller Product",
            description = "Desc",
            price = 100.0,
            salePrice = null,
            mainImageUrl = "img.url",
            additionalImageUrls = emptyList(),
            categoryId = category.id,
            currentStock = 10,
            weightKg = null,
            supplierId = supplier.id,
            costPrice = 50.0,
            isConsigned = false
        )
        val product = client.post("/admin/products") {
            bearerAuth(adminToken)
            contentType(ContentType.Application.Json)
            setBody(productRequest)
        }.body<Product>()

        val address = client.post("/users/me/addresses") {
            bearerAuth(customerToken)
            contentType(ContentType.Application.Json)
            setBody(AddressRequest("Home", "123 Main St", "1A", "10001", "City", "State"))
        }.body<Address>()

        client.post("/cart/add") {
            bearerAuth(customerToken)
            contentType(ContentType.Application.Json)
            setBody(CartItem(product.id, 1))
        }

        val checkoutRequest = CheckoutRequest(
            addressId = address.id,
            resellerSlug = resellerSlug
        )
        val checkoutResponse = client.post("/orders/checkout") {
            bearerAuth(customerToken)
            contentType(ContentType.Application.Json)
            setBody(checkoutRequest)
        }
        assertEquals(HttpStatusCode.Created, checkoutResponse.status, "Checkout with reseller slug failed.")

        // --- PART 3: VERIFY ORDER ATTRIBUTION & ADMIN ACTIONS ---
        val orders = client.get("/orders") { bearerAuth(customerToken) }.body<List<Order>>()
        assertEquals(1, orders.size)
        assertEquals(resellerUser.id, orders.first().resellerId, "Order was not correctly attributed.")

        val updateRequest = ResellerUpdateRequest(resellerSlug, 30.0, false)
        client.put("/admin/resellers/${resellerUser.id}") {
            bearerAuth(adminToken)
            contentType(ContentType.Application.Json)
            setBody(updateRequest)
        }

        client.delete("/admin/resellers/${resellerUser.id}") {
            bearerAuth(adminToken)
        }
    }
}