package routes

import com.example.plugins.BadRequestException
import com.example.plugins.NotFoundException
import data.model.MarketingMaterialRequest
import data.repository.MarketingRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.marketingRouting() {
    val repository: MarketingRepository by inject()

    authenticate("auth-jwt") {
        // Endpoint for Resellers to view resources
        route("/reseller/me/resources") {
            get {
                val principal = call.principal<JWTPrincipal>()!!
                if (principal.getClaim("role", String::class) != "RESELLER") {
                    call.respond(HttpStatusCode.Forbidden, "Reseller role required.")
                    return@get
                }
                val materials = repository.getAllMaterials()
                call.respond(materials)
            }
        }

        // Endpoints for Admins to manage resources
        route("/admin/resources") {
            intercept(ApplicationCallPipeline.Call) {
                val principal = call.principal<JWTPrincipal>()
                if (principal?.getClaim("role", String::class) != "ADMIN") {
                    call.respond(HttpStatusCode.Forbidden, "Administrator role required.")
                    return@intercept finish()
                }
            }

            post {
                val request = call.receive<MarketingMaterialRequest>()
                val newMaterial = repository.addMaterial(request)
                call.respond(HttpStatusCode.Created, newMaterial)
            }

            delete("{id}") {
                val id = call.parameters["id"] ?: throw BadRequestException("Resource ID is missing.")
                val deleted = repository.deleteMaterial(id)
                if (deleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    throw NotFoundException("Resource with ID $id not found.")
                }
            }
        }
    }
}