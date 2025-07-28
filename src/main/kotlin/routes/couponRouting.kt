package routes

import data.model.CouponCreateRequest
import data.repository.CouponRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.couponRouting() {
    val repository: CouponRepository by inject()

    authenticate("auth-jwt") {
        route("/admin/coupons") {
            // Role check for all admin routes
            intercept(ApplicationCallPipeline.Call) {
                val principal = call.principal<JWTPrincipal>()
                if (principal?.getClaim("role", String::class) != "ADMIN") {
                    call.respond(HttpStatusCode.Forbidden, "Administrator role required.")
                    return@intercept finish()
                }
            }

            /**
             * POST /admin/coupons - Create a new discount coupon
             */
            post {
                val request = call.receive<CouponCreateRequest>()
                val newCoupon = repository.createCoupon(request)
                if (newCoupon != null) {
                    call.respond(HttpStatusCode.Created, newCoupon)
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to create coupon.")
                }
            }

            /**
             * GET /admin/coupons - Get a list of all coupons
             */
            get {
                val coupons = repository.getAllCoupons()
                call.respond(coupons)
            }
        }
    }
}