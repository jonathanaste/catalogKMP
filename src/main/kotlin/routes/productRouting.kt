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

    route("/productos") {
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
        route("/admin/productos") {
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
                val request = call.receive<ProductRequest>() // <-- ¡Ahora espera un ProductRequest!

                // 2. Creamos el objeto Product completo, generando el ID en el servidor.
                val productToCreate = Product(
                    id = UUID.randomUUID().toString(),
                    name = request.name,
                    description = request.description,
                    price = request.price,
                    mainImageUrl = request.mainImageUrl,
                    categoryId = request.categoryId,
                    stockQuantity = request.stockQuantity,
                    supplierId = request.supplierId,
                )

                // 3. Pasamos el objeto completo al repositorio para guardarlo.
                val newProduct = repository.addProduct(productToCreate)
                call.respond(HttpStatusCode.Created, newProduct)
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
                // Nuestro plugin StatusPages la capturará y devolverá una respuesta 400 o 500 estandarizada
                val productRequest = call.receive<Product>()
                val updated = repository.updateProduct(id, productRequest)
                if (updated) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    throw com.example.plugins.NotFoundException("Se requiere rol de Administrador.")
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