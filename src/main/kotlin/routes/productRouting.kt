package routes

import com.example.data.model.ProductRequest
import com.example.data.repository.ProductRepository
import com.example.plugins.ForbiddenException
import io.ktor.http.*
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.plugins.origin
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.io.File
import java.util.UUID

fun Route.productRouting() {
    val repository: ProductRepository by inject()

    route("/products") {
        // GET /products - Get all products (Public)
        get {
            // The repository now directly returns the correct Product DTO.
            val products = repository.getAllProducts()
            call.respond(products)
        }

        // GET /products/{id} - Get a single product by its ID (Public)
        get("{id}") {
            val id = call.parameters["id"] ?: throw BadRequestException("Product ID is missing.")
            // The repository now directly returns the correct Product DTO.
            val product = repository.getProductById(id) ?: throw NotFoundException("Product with id $id not found.")
            call.respond(product)
        }
    }

    // --- Admin Routes ---
    authenticate("auth-jwt") {
        route("/admin/products") {

            // POST /admin/products - Create a new product
            post {
                val principal = call.principal<JWTPrincipal>()
                if (principal?.getClaim("role", String::class) != "ADMIN") {
                    throw ForbiddenException("Administrator role required.")
                }

                // Receive the updated ProductRequest DTO.
                val request = call.receive<ProductRequest>()

                // The repository now handles the creation logic.
                val newProduct = repository.addProduct(request)
                call.respond(HttpStatusCode.Created, newProduct)
            }

            // PUT /admin/products/{id} - Update an existing product
            post {
                val principal = call.principal<JWTPrincipal>()
                if (principal?.getClaim("role", String::class) != "ADMIN") {
                    throw ForbiddenException("Administrator role required.")
                }

                // --- NEW MULTIPART HANDLING LOGIC ---
                val multipartData = call.receiveMultipart()
                var productRequestData: MutableMap<String, Any> = mutableMapOf()
                var mainImageUrl: String? = null
                val additionalImageUrls = mutableListOf<String>()

                multipartData.forEachPart { part ->
                    // **SAFE NAME HANDLING**
                    val partName = part.name
                    if (!partName.isNullOrBlank()) {
                        when (part) {
                            is PartData.FileItem -> {
                                val fileName = part.originalFileName ?: "unknown.jpg"
                                val fileExtension = File(fileName).extension
                                val uniqueFileName = "${UUID.randomUUID()}.$fileExtension"
                                val file = File("uploads/$uniqueFileName")

                                part.streamProvider().use { its ->
                                    file.outputStream().buffered().use {
                                        its.copyTo(it)
                                    }
                                }

                                val fileUrl = "${call.request.origin.scheme}://${call.request.origin.host}:${call.request.origin.port}/static/$uniqueFileName"

                                // Use the safe partName variable here
                                if (partName == "mainImage") {
                                    mainImageUrl = fileUrl
                                } else if (partName == "additionalImages") {
                                    additionalImageUrls.add(fileUrl)
                                }
                            }
                            is PartData.FormItem -> {
                                // Use the safe partName variable here
                                productRequestData[partName] = part.value
                            }
                            else -> {}
                        }
                    }
                    part.dispose()
                }

                if (mainImageUrl == null) {
                    throw BadRequestException("Main image is required.")
                }

                // --- Create the ProductRequest from the parsed data ---
                // This part requires careful type casting and validation
                val request = ProductRequest(
                    sku = productRequestData["sku"] as? String ?: throw BadRequestException("SKU is required."),
                    name = productRequestData["name"] as? String ?: throw BadRequestException("Name is required."),
                    description = productRequestData["description"] as? String ?: throw BadRequestException("Description is required."),
                    price = (productRequestData["price"] as? String)?.toDoubleOrNull() ?: throw BadRequestException("Valid price is required."),
                    salePrice = (productRequestData["salePrice"] as? String)?.toDoubleOrNull(),
                    mainImageUrl = mainImageUrl!!,
                    additionalImageUrls = additionalImageUrls,
                    categoryId = productRequestData["categoryId"] as? String ?: throw BadRequestException("Category ID is required."),
                    currentStock = (productRequestData["currentStock"] as? String)?.toIntOrNull() ?: throw BadRequestException("Valid stock is required."),
                    weightKg = (productRequestData["weightKg"] as? String)?.toDoubleOrNull(),
                    supplierId = productRequestData["supplierId"] as? String,
                    costPrice = (productRequestData["costPrice"] as? String)?.toDoubleOrNull() ?: throw BadRequestException("Valid cost price is required."),
                    isConsigned = (productRequestData["isConsigned"] as? String)?.toBoolean() ?: false
                )

                val newProduct = repository.addProduct(request)
                call.respond(HttpStatusCode.Created, newProduct)
            }

            // DELETE /admin/products/{id} - Delete a product
            delete("{id}") {
                val principal = call.principal<JWTPrincipal>()
                if (principal?.getClaim("role", String::class) != "ADMIN") {
                    throw ForbiddenException("Administrator role required.")
                }

                val id = call.parameters["id"] ?: throw BadRequestException("Product ID is missing.")
                val deleted = repository.deleteProduct(id)

                if (deleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    throw NotFoundException("Product with id $id not found.")
                }
            }
        }
    }
}