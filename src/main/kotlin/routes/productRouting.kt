package routes

import com.example.data.model.ProductRequest
import com.example.data.repository.ProductRepository
import com.example.plugins.ForbiddenException
import io.ktor.http.*
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

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

        get("/summary") {
            val productSummaries = repository.getAllProductSummaries()
            call.respond(productSummaries)
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
            put("{id}") {
                val principal = call.principal<JWTPrincipal>()
                if (principal?.getClaim("role", String::class) != "ADMIN") {
                    throw ForbiddenException("Administrator role required.")
                }

                val id = call.parameters["id"] ?: throw BadRequestException("Product ID is missing.")

                // Receive the updated ProductRequest DTO for the update operation.
                val request = call.receive<ProductRequest>()

                val updated = repository.updateProduct(id, request)
                if (updated) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    throw NotFoundException("Product with id $id not found.")
                }
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