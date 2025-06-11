package routes

import com.example.data.model.Category
import com.example.data.model.CategoryRequest
import com.example.data.repository.CategoryRepository
import com.example.plugins.BadRequestException
import com.example.plugins.ForbiddenException
import com.example.plugins.NotFoundException
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
            val id = call.parameters["id"] ?: throw BadRequestException("ID de categoría no encontrado")

            val category =
                repository.getCategoryById(id) ?: throw NotFoundException("No se encontró categoría con id $id")
            call.respond(category)
        }
    }

    // Rutas de Administración para Categorías
    authenticate("auth-jwt") {
        route("/admin/categorias") {

            // GET /categorias - Público
            get {
                val categories = repository.getAllCategories()
                call.respond(categories)
            }

            // GET /categorias/{id} - Público
            get("{id}") {
                val id = call.parameters["id"] ?: throw BadRequestException("ID de categoría no encontrado")

                val category =
                    repository.getCategoryById(id) ?: throw NotFoundException("No se encontró categoría con id $id")
                call.respond(category)
            }

            // POST /admin/categorias - Protegido
            post {
                val principal = call.principal<JWTPrincipal>()
                if (principal?.getClaim("role", String::class) != "ADMIN") {
                    throw ForbiddenException("Se requiere rol de Administrador.")
                }

                // Si call.receive falla, Ktor lanzará una excepción.
                // Nuestro plugin StatusPages la capturará y devolverá una respuesta 400 o 500 estandarizada
                val request = call.receive<CategoryRequest>()
                val newCategory = repository.addCategory(request)
                call.respond(HttpStatusCode.Created, newCategory)
            }

            // PUT /admin/categorias/{id} - Protegido
            put("{id}") {
                val principal = call.principal<JWTPrincipal>()
                if (principal?.getClaim("role", String::class) != "ADMIN") {
                    throw ForbiddenException("Se requiere rol de Administrador.")
                }

                val id = call.parameters["id"] ?: throw BadRequestException("ID de categoría no encontrado")
                val categoryRequest = call.receive<Category>()

                val updated = repository.updateCategory(id, categoryRequest)
                if (updated) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    throw NotFoundException("No se encontró categoría con id $id")
                }
            }

            // DELETE /admin/categorias/{id} - Protegido
            delete("{id}") {
                val principal = call.principal<JWTPrincipal>()
                if (principal?.getClaim("role", String::class) != "ADMIN") {
                    throw ForbiddenException("Se requiere rol de Administrador.")
                }

                val id = call.parameters["id"] ?: throw BadRequestException("ID de categoría no encontrado")

                val deleted = repository.deleteCategory(id)
                if (deleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    throw NotFoundException("No se encontró categoría con id $id")
                }
            }
        }
    }
}