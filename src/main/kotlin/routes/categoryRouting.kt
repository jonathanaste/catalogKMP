package routes

import com.example.data.model.Category
import com.example.data.repository.CategoryRepository
import com.example.data.repository.CategoryRepositoryImpl
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.categoryRouting() {
    val repository: CategoryRepository = CategoryRepositoryImpl()

    // GET /categorias - Endpoint público para obtener todas las categorías 
    route("/categorias") {
        get {
            val categories = repository.getAllCategories()
            call.respond(categories)
        }
    }

    // POST /admin/categorias - Endpoint para crear una nueva categoría
    route("/admin/categorias") {
        post {
            // TODO: Proteger esta ruta para que solo la usen los ADMIN
            try {
                val categoryRequest = call.receive<Category>()
                val newCategory = repository.addCategory(categoryRequest)
                call.respond(HttpStatusCode.Created, newCategory)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Datos de categoría inválidos: ${e.message}")
            }
        }
    }
}