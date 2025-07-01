package routes

import com.example.data.model.ProductRequest
import com.example.data.model.Product
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
import java.util.UUID

fun Route.productRouting() {
    // Instanciamos el repositorio. En una app más grande, usaríamos inyección de dependencias (Koin).
    val repository: ProductRepository by inject()

    route("/products") {
        // GET /productos - Obtener todos los productos
        get {
            val products = repository.getAllProducts()
            call.respond(products)
        }

        // GET /productos/{id} - Obtener un producto por su ID
        get("{id}") {
            val id = call.parameters["id"] ?: throw BadRequestException("ID de producto no encontrado")

            val product = repository.getProductById(id) ?: throw NotFoundException("No se encontró producto con id $id")

            call.respond(product)
        }
    }

    // Rutas de Administración - Protegidas en el futuro
    authenticate("auth-jwt") {
        route("/admin/products") {
            get {
                val products = repository.getAllProducts()
                call.respond(products)
            }

            // GET /productos/{id} - Obtener un producto por su ID
            get("{id}") {
                val id = call.parameters["id"] ?: throw BadRequestException("ID de producto no encontrado")

                val product =
                    repository.getProductById(id) ?: throw NotFoundException("No se encontró producto con id $id")

                call.respond(product)
            }
            // POST /admin/productos - Crear un nuevo producto
            post {
                // --- INICIO DE LA VERIFICACIÓN DE ROL ---
                val principal = call.principal<JWTPrincipal>()
                if (principal?.getClaim("role", String::class) != "ADMIN") {
                    throw ForbiddenException("Se requiere rol de Administrador.")
                }
                // --- FIN DE LA VERIFICACIÓN DE ROL ---

                // Si call.receive falla, Ktor lanzará una excepción.
                // Nuestro plugin StatusPages la capturará y devolverá una respuesta 400 o 500 estandarizada
                val request = call.receive<ProductRequest>()

                // Map ProductRequest to Product domain model, including variants
                val productToCreate = Product(
                    id = UUID.randomUUID().toString(), // ID for the main product
                    name = request.name,
                    description = request.description,
                    price = request.price,
                    mainImageUrl = request.mainImageUrl,
                    categoryId = request.categoryId,
                    stockQuantity = request.stockQuantity,
                    supplierId = request.supplierId,
                    costPrice = request.costPrice,
                    isConsigned = request.isConsigned,
                    hasVariants = request.variants?.isNotEmpty() ?: false,
                    variants = request.variants?.map { vr ->
                        ProductVariant(
                            id = UUID.randomUUID().toString(), // Generate ID for each new variant
                            productId = "", // Will be set by repository or this should be the main product ID
                            sku = vr.sku,
                            name = vr.name,
                            price = vr.price,
                            stockQuantity = vr.stockQuantity,
                            attributes = vr.attributes,
                            imageUrl = vr.imageUrl
                        )
                    }
                )
                // The productId in ProductVariant should ideally be the productToCreate.id.
                // Let's ensure the repository handles setting this if it's passed as empty,
                // or we set it here. The repository `addProduct` expects `product.id` to be used.
                // So, the ProductVariant's productId should be productToCreate.id
                val finalProductToCreate = productToCreate.copy(
                    variants = productToCreate.variants?.map { it.copy(productId = productToCreate.id) }
                )

                val newProduct = repository.addProduct(finalProductToCreate)
                // The repository addProduct returns a Product. We should ideally return a ProductResponse.
                // For now, let's assume the client can handle the Product model or this will be refined.
                // To return ProductResponse, we'd need to fetch it again or enhance repository.
                call.respond(HttpStatusCode.Created, newProduct) // Consider mapping to ProductResponse
            }

            // PUT /admin/productos/{id} - Actualizar un producto existente
            put("{id}") {
                val principal = call.principal<JWTPrincipal>()
                val userRole = principal?.getClaim("role", String::class)
                if (userRole != "ADMIN") {
                    throw ForbiddenException("Se requiere rol de Administrador.")
                }

                val id = call.parameters["id"] ?: return@put call.respond(
                    HttpStatusCode.BadRequest,
                    "ID de producto faltante"
                )

                // Si call.receive falla, Ktor lanzará una excepción.
                val receivedProduct = call.receive<Product>() // Client sends a Product object for update

                // Ensure productId in variants matches the main product ID.
                // Generate IDs for any new variants (those without an ID or with a placeholder).
                // The repository update logic expects all incoming variants to have IDs.
                val processedVariants = receivedProduct.variants?.map { variant ->
                    val variantId = if (variant.id.isNullOrBlank() || variant.id == "new") UUID.randomUUID().toString() else variant.id
                    variant.copy(id = variantId, productId = id)
                }

                val productToUpdate = receivedProduct.copy(
                    id = id, // Ensure the ID from the path is used
                    hasVariants = processedVariants?.isNotEmpty() ?: false,
                    variants = processedVariants
                )

                val updated = repository.updateProduct(id, productToUpdate)
                if (updated) {
                    // Consider returning the updated ProductResponse
                    call.respond(HttpStatusCode.OK, "Producto actualizado correctamente.") // Returning ProductResponse would be better
                } else {
                    // The original code threw NotFoundException with a generic admin message.
                    // It should be specific to product not found or update failure.
                    throw NotFoundException("Producto con id $id no encontrado o no se pudo actualizar.")
                }
            }

            // DELETE /admin/productos/{id} - Eliminar un producto
            delete("{id}") {
                val principal = call.principal<JWTPrincipal>()
                val userRole = principal?.getClaim("role", String::class)
                if (userRole != "ADMIN") {
                    throw ForbiddenException("Se requiere rol de Administrador.")
                }
                val id = requireNotNull(call.parameters["id"]) {
                    "El ID del producto no puede ser nulo."
                }

                val deleted = repository.deleteProduct(id)

                if (!deleted) {
                    throw NotFoundException("Producto con id $id no encontrado.")
                }

                call.respond(HttpStatusCode.NoContent)

            }
        }
    }
}