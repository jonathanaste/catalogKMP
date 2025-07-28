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
    fun `Test full user flow for Phase 2 features`() = testApplication {
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
        val adminToken = getAuthToken("admin@example.com", "adminpass")
        val uniqueSuffix = System.currentTimeMillis()

        val category = client.post("/admin/categories") {
            bearerAuth(adminToken)
            contentType(ContentType.Application.Json)
            setBody(CategoryRequest("Test Category - $uniqueSuffix", "http://example.com/category.png"))
        }.body<Category>()

        val productRequest = ProductRequest(
            sku = "TEST-SKU-$uniqueSuffix",
            name = "Testable Super Serum",
            description = "A product for testing.",
            price = 1000.0,
            salePrice = 800.0, // Using salePrice
            mainImageUrl = "http://example.com/product.png",
            additionalImageUrls = listOf("http://example.com/extra.png"),
            categoryId = category.id,
            currentStock = 100,
            weightKg = 0.5,
            supplierId = null,
            costPrice = 500.0,
            isConsigned = false
        )
        val product = client.post("/admin/products") {
            bearerAuth(adminToken)
            contentType(ContentType.Application.Json)
            setBody(productRequest)
        }.body<Product>()

        val percentageCoupon = client.post("/admin/coupons") {
            bearerAuth(adminToken)
            contentType(ContentType.Application.Json)
            setBody(CouponCreateRequest("SUMMER25-$uniqueSuffix", "25% off", "PERCENTAGE", 25.0))
        }.body<Coupon>()

        val fixedCoupon = client.post("/admin/coupons") {
            bearerAuth(adminToken)
            contentType(ContentType.Application.Json)
            setBody(CouponCreateRequest("500OFF-$uniqueSuffix", "500 off", "FIXED_AMOUNT", 500.0))
        }.body<Coupon>()

        // --- PART 1: CLIENT USER & SETUP ---
        val userEmail = "coupon-tester-$uniqueSuffix@example.com"
        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest(userEmail, "password123", "Coupon", "Tester"))
        }
        val userToken = getAuthToken(userEmail, "password123")

        val address = client.post("/users/me/addresses") {
            bearerAuth(userToken)
            contentType(ContentType.Application.Json)
            setBody(AddressRequest("Home", "123 Test St", "Apt 4B", "10001", "Testville", "Testland"))
        }.body<Address>()

        // --- PART 2: TEST CHECKOUT WITH COUPONS ---
        // Subtotal will be 1600.0 (800.0 * 2) because salePrice is used.
        client.post("/cart/add") {
            bearerAuth(userToken)
            contentType(ContentType.Application.Json)
            setBody(CartItem(productId = product.id, quantity = 2))
        }
        client.post("/orders/checkout") {
            bearerAuth(userToken)
            contentType(ContentType.Application.Json)
            setBody(CheckoutRequest(addressId = address.id, couponCode = percentageCoupon.code))
        }

        client.post("/cart/add") {
            bearerAuth(userToken)
            contentType(ContentType.Application.Json)
            setBody(CartItem(productId = product.id, quantity = 2))
        }
        client.post("/orders/checkout") {
            bearerAuth(userToken)
            contentType(ContentType.Application.Json)
            setBody(CheckoutRequest(addressId = address.id, couponCode = fixedCoupon.code))
        }

        // --- PART 3: VERIFY ORDER TOTALS ---
        val orders = client.get("/orders") {
            bearerAuth(userToken)
        }.body<List<Order>>()

        assertEquals(2, orders.size)

        // --- CORRECTED ASSERTIONS ---
        val fixedDiscountOrder = orders.find { it.couponCode == fixedCoupon.code }
        assertNotNull(fixedDiscountOrder)
        assertEquals(1100.0, fixedDiscountOrder.total) // Correct total: (800 * 2) - 500 = 1100

        val percentDiscountOrder = orders.find { it.couponCode == percentageCoupon.code }
        assertNotNull(percentDiscountOrder)
        assertEquals(1200.0, percentDiscountOrder.total) // Correct total: (800 * 2) * 0.75 = 1200
    }
}