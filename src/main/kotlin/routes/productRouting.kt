package routes

import com.example.data.model.ProductRequest
import com.example.data.model.Product
import com.example.data.repository.ProductRepository
import com.example.data.repository.ProductRepositoryImpl
import io.ktor.http.*
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.productRouting() {
    // Instanciamos el repositorio. En una app más grande, usaríamos inyección de dependencias (Koin).
    val repository: ProductRepository = ProductRepositoryImpl()

    route("/productos") {
        // GET /productos - Obtener todos los productos 
        get {
            val products = repository.getAllProducts()
            call.respond(products)
        }

        // GET /productos/{id} - Obtener un producto por su ID 
        get("{id}") {
            val id = call.parameters["id"] ?: return@get call.respondText(
                "ID de producto no encontrado",
                status = HttpStatusCode.BadRequest
            )

            val product = repository.getProductById(id)
            if (product != null) {
                call.respond(product)
            } else {
                call.respondText(
                    "No se encontró producto con id $id",
                    status = HttpStatusCode.NotFound
                )
            }
        }
    }

    // Rutas de Administración - Protegidas en el futuro
    authenticate("auth-jwt") {
        route("/admin/productos") {
            // POST /admin/productos - Crear un nuevo producto
            post {
                // --- INICIO DE LA VERIFICACIÓN DE ROL ---
                val principal = call.principal<JWTPrincipal>()
                val userRole = principal?.getClaim("role", String::class)

                if (userRole != "ADMIN") {
                    call.respond(HttpStatusCode.Forbidden, "Acceso denegado. Se requiere rol de Administrador.")
                    return@post
                }
                // --- FIN DE LA VERIFICACIÓN DE ROL ---

                try {
                    val request = call.receive<ProductRequest>() // <-- ¡Ahora espera un ProductRequest!

                    // 2. Creamos el objeto Product completo, generando el ID en el servidor.
                    val productToCreate = Product(
                        id = UUID.randomUUID().toString(),
                        name = request.name,
                        description = request.description,
                        price = request.price,
                        mainImageUrl = request.mainImageUrl,
                        categoryId = request.categoryId,
                        inStock = request.inStock,
                    )

                    // 3. Pasamos el objeto completo al repositorio para guardarlo.
                    val newProduct = repository.addProduct(productToCreate)

                    call.respond(HttpStatusCode.Created, newProduct)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "Datos de producto inválidos: ${e.message}")
                }
            }

            // PUT /admin/productos/{id} - Actualizar un producto existente
            put("{id}") {
                val principal = call.principal<JWTPrincipal>()
                val userRole = principal?.getClaim("role", String::class)
                if (userRole != "ADMIN") {
                    return@put call.respond(HttpStatusCode.Forbidden, "Acceso denegado.")
                }

                val id = call.parameters["id"] ?: return@put call.respond(
                    HttpStatusCode.BadRequest,
                    "ID de producto faltante"
                )

                try {
                    val productRequest = call.receive<Product>()
                    val updated = repository.updateProduct(id, productRequest)
                    if (updated) {
                        call.respond(HttpStatusCode.OK)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Producto no encontrado")
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "Datos de producto inválidos: ${e.message}")
                }
            }

            // DELETE /admin/productos/{id} - Eliminar un producto
            delete("{id}") {
                val principal = call.principal<JWTPrincipal>()
                val userRole = principal?.getClaim("role", String::class)
                if (userRole != "ADMIN") {
                    return@delete call.respond(HttpStatusCode.Forbidden, "Acceso denegado.")
                }
                val id = call.parameters["id"] ?: return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    "ID de producto faltante"
                )

                val deleted = repository.deleteProduct(id)
                if (deleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Producto no encontrado")
                }
            }
        }
    }
}