package routes

import com.example.plugins.ConflictException
import data.model.ResellerCreateRequest
import data.model.ResellerUpdateRequest
import data.repository.ResellerRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.resellerRouting() {
    val repository: ResellerRepository by inject()

    authenticate("auth-jwt") {
        route("/admin/resellers") {
            // Interceptor to ensure only ADMINs can access these routes
            intercept(ApplicationCallPipeline.Call) {
                val principal = call.principal<JWTPrincipal>()
                if (principal?.getClaim("role", String::class) != "ADMIN") {
                    call.respond(HttpStatusCode.Forbidden, "Administrator role required.")
                    return@intercept finish()
                }
            }

            /**
             * GET /admin/resellers - Get a list of all resellers
             */
            get {
                val resellers = repository.getAllResellers()
                call.respond(resellers)
            }

            /**
             * POST /admin/resellers - Create a new reseller
             */
            post {
                val request = call.receive<ResellerCreateRequest>()
                val newReseller = repository.createReseller(request)
                    ?: throw ConflictException("A user with that email or store slug already exists.")

                // We respond with the full user object, including the generated profile
                call.respond(HttpStatusCode.Created, newReseller)
            }

            get("{id}") {
                val id = call.parameters["id"] ?: throw BadRequestException("Reseller ID is missing.")
                val reseller = repository.findResellerById(id)
                    ?: throw NotFoundException("Reseller with ID $id not found.")
                call.respond(reseller)
            }

            /**
             * PUT /admin/resellers/{id} - Update a reseller's profile
             */
            put("{id}") {
                val id = call.parameters["id"] ?: throw BadRequestException("Reseller ID is missing.")
                val request = call.receive<ResellerUpdateRequest>()
                val updated = repository.updateReseller(id, request)
                if (updated) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    throw NotFoundException("Reseller with ID $id not found.")
                }
            }

            /**
             * DELETE /admin/resellers/{id} - Delete a reseller
             */
            delete("{id}") {
                val id = call.parameters["id"] ?: throw BadRequestException("Reseller ID is missing.")
                val deleted = repository.deleteReseller(id)
                if (deleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    throw NotFoundException("Reseller with ID $id not found.")
                }
            }
        }
    }
}