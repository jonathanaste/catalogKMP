package routes

import com.example.data.model.CreateSupplierRequest
import com.example.data.model.Supplier
import com.example.data.repository.SupplierRepository
import com.example.plugins.ForbiddenException
import com.example.plugins.NotFoundException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.supplierRouting() {
    val repository: SupplierRepository by inject()

    authenticate("auth-jwt") {
        route("/admin/suppliers") {

            // GET /admin/suppliers - Obtener todos los proveedores
            get {
                call.respond(repository.getAllSuppliers())
            }

            // GET /admin/suppliers/{id} - Obtener un proveedor por ID
            get("{id}") {
                val id = requireNotNull(call.parameters["id"]) { "El ID del proveedor no puede ser nulo." }
                val supplier = repository.getSupplierById(id) ?: throw NotFoundException("Proveedor no encontrado.")
                call.respond(supplier)
            }

            // POST /admin/suppliers - Crear un nuevo proveedor
            post {
                val principal = call.principal<JWTPrincipal>()
                if (principal?.getClaim("role", String::class) != "ADMIN") {
                    throw ForbiddenException("Se requiere rol de Administrador.")
                }

                val request = call.receive<CreateSupplierRequest>()
                val newSupplier = repository.addSupplier(request)
                call.respond(HttpStatusCode.Created, newSupplier)
            }

            // PUT /admin/suppliers/{id} - Actualizar un proveedor
            put("{id}") {
                val principal = call.principal<JWTPrincipal>()
                if (principal?.getClaim("role", String::class) != "ADMIN") {
                    throw ForbiddenException("Se requiere rol de Administrador.")
                }

                val id = requireNotNull(call.parameters["id"]) { "El ID del proveedor no puede ser nulo." }
                val request = call.receive<Supplier>()

                val updated = repository.updateSupplier(id, request)
                if (!updated) throw NotFoundException("Proveedor no encontrado para actualizar.")

                call.respond(HttpStatusCode.OK)
            }

            // DELETE /admin/suppliers/{id} - Eliminar un proveedor
            delete("{id}") {
                val principal = call.principal<JWTPrincipal>()
                if (principal?.getClaim("role", String::class) != "ADMIN") {
                    throw ForbiddenException("Se requiere rol de Administrador.")
                }

                val id = requireNotNull(call.parameters["id"]) { "El ID del proveedor no puede ser nulo." }

                val deleted = repository.deleteSupplier(id)
                if (!deleted) throw NotFoundException("Proveedor no encontrado para eliminar.")

                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}