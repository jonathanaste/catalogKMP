package routes

import com.example.data.model.Category
import com.example.data.repository.CategoryRepository
import io.ktor.http.*
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import kotlin.getValue

fun Route.categoryRouting() {
    val repository: CategoryRepository by inject()

    route("/categorias") {
        // GET /categorias - Público
        get {
            val categories = repository.getAllCategories()
            call.respond(categories)
        }

        // GET /categorias/{id} - Público
        get("{id}") {
            val id = call.parameters["id"] ?: return@get call.respondText(
                "ID de categoría no encontrado",
                status = HttpStatusCode.BadRequest
            )

            val category = repository.getCategoryById(id)
            if (category != null) {
                call.respond(category)
            } else {
                call.respondText(
                    "No se encontró categoría con id $id",
                    status = HttpStatusCode.NotFound
                )
            }
        }
    }

    // Rutas de Administración para Categorías
    authenticate("auth-jwt") {
        route("/admin/categorias") {
            // POST /admin/categorias - Protegido
            post {
                val principal = call.principal<JWTPrincipal>()
                if (principal?.getClaim("role", String::class) != "ADMIN") {
                    return@post call.respond(HttpStatusCode.Forbidden)
                }

                // Si call.receive falla, Ktor lanzará una excepción.
                // Nuestro plugin StatusPages la capturará y devolverá una respuesta 400 o 500 estandarizada
                val categoryRequest = call.receive<Category>()
                val newCategory = repository.addCategory(categoryRequest)
                call.respond(HttpStatusCode.Created, newCategory)

            }

            // PUT /admin/categorias/{id} - Protegido
            put("{id}") {
                val principal = call.principal<JWTPrincipal>()
                if (principal?.getClaim("role", String::class) != "ADMIN") {
                    return@put call.respond(HttpStatusCode.Forbidden)
                }

                val id = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest)
                val categoryRequest = call.receive<Category>()

                val updated = repository.updateCategory(id, categoryRequest)
                if (updated) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            // DELETE /admin/categorias/{id} - Protegido
            delete("{id}") {
                val principal = call.principal<JWTPrincipal>()
                if (principal?.getClaim("role", String::class) != "ADMIN") {
                    return@delete call.respond(HttpStatusCode.Forbidden)
                }

                val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)

                val deleted = repository.deleteCategory(id)
                if (deleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}