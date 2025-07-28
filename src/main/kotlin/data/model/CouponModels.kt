package data.model

import kotlinx.serialization.Serializable

@Serializable
data class Coupon(
    val code: String,
    val description: String,
    val discountType: String, // "PERCENTAGE" or "FIXED_AMOUNT"
    val discountValue: Double,
    val expirationDate: Long? = null,
    val isActive: Boolean
)

/**
 * Represents the JSON request body for creating a new discount coupon.
 * This is used by the POST /admin/coupons endpoint.
 */
@Serializable
data class CouponCreateRequest(
    val code: String,
    val description: String,
    val discountType: String,
    val discountValue: Double,
    val expirationDate: Long? = null,
    val usageLimit: Int? = null
)