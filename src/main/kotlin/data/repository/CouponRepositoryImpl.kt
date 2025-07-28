package data.repository

import com.example.data.model.CouponsTable
import com.example.plugins.DatabaseFactory.dbQuery
import data.model.Coupon
import data.model.CouponCreateRequest
import org.jetbrains.exposed.sql.*

class CouponRepositoryImpl : CouponRepository {

    private fun resultRowToCoupon(row: ResultRow) = Coupon(
        code = row[CouponsTable.code],
        description = row[CouponsTable.description],
        discountType = row[CouponsTable.discountType],
        discountValue = row[CouponsTable.discountValue],
        expirationDate = row[CouponsTable.expirationDate],
        isActive = row[CouponsTable.isActive]
    )

    override suspend fun createCoupon(request: CouponCreateRequest): Coupon? {
        return dbQuery {
            CouponsTable.insert {
                it[code] = request.code
                it[description] = request.description
                it[discountType] = request.discountType
                it[discountValue] = request.discountValue
                it[expirationDate] = request.expirationDate
                it[usageLimit] = request.usageLimit
                it[isActive] = true
            }.resultedValues?.firstOrNull()?.let(::resultRowToCoupon)
        }
    }

    override suspend fun findActiveCouponByCode(code: String): Coupon? {
        return dbQuery {
            CouponsTable.selectAll()
                .where { (CouponsTable.code eq code) and (CouponsTable.isActive eq true) }
                .map(::resultRowToCoupon)
                .singleOrNull()
        }
    }

    override suspend fun getAllCoupons(): List<Coupon> = dbQuery {
        CouponsTable.selectAll().map(::resultRowToCoupon)
    }
}