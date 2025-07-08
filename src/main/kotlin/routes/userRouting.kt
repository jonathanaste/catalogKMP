package routes

import com.example.data.repository.UserRepository
import com.example.plugins.NotFoundException
import data.model.AddressRequest
import data.repository.AddressRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.userRouting() {
    val userRepository: UserRepository by inject()
    val addressRepository: AddressRepository by inject()

    authenticate("auth-jwt") {
        route("/users/me") {
            // GET /users/me - Get the authenticated user's profile
            get {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.getClaim("userId", String::class)!!
                val user = userRepository.findUserByEmail(principal.getClaim("email", String::class)!!)
                    ?: throw NotFoundException("User not found.")

                // Separately load addresses and combine them
                val addresses = addressRepository.getAddressesForUser(userId)
                val userWithAddresses = user.copy(addresses = addresses)

                call.respond(userWithAddresses)
            }

            // --- Address Management ---
            route("/addresses") {
                // GET /users/me/addresses - List the user's addresses
                get {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.getClaim("userId", String::class)!!
                    val addresses = addressRepository.getAddressesForUser(userId)
                    call.respond(addresses)
                }

                // POST /users/me/addresses - Add a new address
                post {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.getClaim("userId", String::class)!!
                    val request = call.receive<AddressRequest>()
                    val newAddress = addressRepository.addAddressForUser(userId, request)
                    call.respond(HttpStatusCode.Created, newAddress)
                }

                // PUT /users/me/addresses/{id} - Modify an existing address
                put("{id}") {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.getClaim("userId", String::class)!!
                    val addressId = call.parameters["id"] ?: throw IllegalArgumentException("Address ID is missing")
                    val request = call.receive<AddressRequest>()

                    val updated = addressRepository.updateAddressForUser(userId, addressId, request)
                    if (updated) {
                        call.respond(HttpStatusCode.OK)
                    } else {
                        throw NotFoundException("Address not found or you don't have permission to edit it.")
                    }
                }

                // DELETE /users/me/addresses/{id} - Delete an address
                delete("{id}") {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.getClaim("userId", String::class)!!
                    val addressId = call.parameters["id"] ?: throw IllegalArgumentException("Address ID is missing")

                    val deleted = addressRepository.deleteAddressForUser(userId, addressId)
                    if (deleted) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        throw NotFoundException("Address not found or you don't have permission to delete it.")
                    }
                }
            }
        }
    }
}