package data.repository

import data.model.Coupon
import data.model.CouponCreateRequest

interface CouponRepository {
    suspend fun createCoupon(request: CouponCreateRequest): Coupon?
    suspend fun findActiveCouponByCode(code: String): Coupon?
    suspend fun getAllCoupons(): List<Coupon>
}