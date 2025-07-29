package data.repository

import com.example.data.model.OrdersTable
import com.example.data.model.ResellerProfilesTable
import com.example.data.model.User
import com.example.data.model.UsersTable
import com.example.data.repository.UserRepository
import com.example.plugins.DatabaseFactory.dbQuery
import data.model.ResellerCreateRequest
import data.model.ResellerCustomer
import data.model.ResellerDashboardResponse
import data.model.ResellerProfile
import data.model.ResellerUpdateRequest
import data.model.SimpleOrderSummary
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import org.mindrot.jbcrypt.BCrypt
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*
import org.jetbrains.exposed.sql.sum

class ResellerRepositoryImpl(
    private val userRepository: UserRepository
) : ResellerRepository {

    override suspend fun createReseller(request: ResellerCreateRequest): User? {
        val newUserId = UUID.randomUUID().toString()
        val temporaryPassword = UUID.randomUUID().toString().take(12) // Generate a random initial password
        val passwordHash = BCrypt.hashpw(temporaryPassword, BCrypt.gensalt())

        dbQuery {
            // Check for existing email or slug to prevent conflicts
            val emailExists = UsersTable.selectAll().where { UsersTable.email eq request.email }.count() > 0
            val slugExists =
                ResellerProfilesTable.selectAll()
                    .where { ResellerProfilesTable.uniqueStoreSlug eq request.uniqueStoreSlug }
                    .count() > 0

            if (emailExists || slugExists) {
                return@dbQuery null // Return null to indicate conflict
            }

            UsersTable.insert {
                it[id] = newUserId
                it[email] = request.email
                it[this.passwordHash] = passwordHash
                it[firstName] = request.firstName
                it[lastName] = request.lastName
                it[phone] = request.phone
                it[role] = "RESELLER"
            }

            ResellerProfilesTable.insert {
                it[userId] = newUserId
                it[uniqueStoreSlug] = request.uniqueStoreSlug
                it[commissionRate] = request.commissionRate ?: 20.0
            }
        }
        // After creation, fetch the full user object to return
        return userRepository.findUserByEmail(request.email)
    }

    override suspend fun getAllResellers(): List<User> = dbQuery {
        // --- CORRECTED JOIN SYNTAX ---
        (UsersTable innerJoin ResellerProfilesTable)
            .selectAll().where { UsersTable.role eq "RESELLER" }
            .map {
                // This is a simplified mapping; a shared resultRowToUser function is better
                User(
                    id = it[UsersTable.id],
                    email = it[UsersTable.email],
                    firstName = it[UsersTable.firstName],
                    lastName = it[UsersTable.lastName],
                    phone = it[UsersTable.phone],
                    role = it[UsersTable.role],
                    resellerProfile = ResellerProfile(
                        userId = it[ResellerProfilesTable.userId],
                        uniqueStoreSlug = it[ResellerProfilesTable.uniqueStoreSlug],
                        commissionRate = it[ResellerProfilesTable.commissionRate],
                        isActive = it[ResellerProfilesTable.isActive]
                    )
                )
            }
    }

    override suspend fun findResellerById(userId: String): User? {
        val user = userRepository.findUserByEmail(
            dbQuery {
                UsersTable.selectAll().where { UsersTable.id eq userId and (UsersTable.role eq "RESELLER") }
                    .map { it[UsersTable.email] }
                    .singleOrNull() ?: return@dbQuery null
            } ?: return null
        )
        // We ensure the user is a reseller and has a profile.
        return if (user?.role == "RESELLER" && user.resellerProfile != null) user else null
    }

    override suspend fun updateReseller(userId: String, request: ResellerUpdateRequest): Boolean = dbQuery {
        ResellerProfilesTable.update({ ResellerProfilesTable.userId eq userId }) {
            it[uniqueStoreSlug] = request.uniqueStoreSlug
            it[commissionRate] = request.commissionRate
            it[isActive] = request.isActive
        } > 0
    }

    override suspend fun deleteReseller(userId: String): Boolean = dbQuery {
        // Because of the ON DELETE CASCADE constraint, deleting the user
        // will automatically delete their reseller_profile, addresses, etc.
        UsersTable.deleteWhere { UsersTable.id eq userId and (UsersTable.role eq "RESELLER") } > 0
    }

    override suspend fun findActiveResellerBySlug(slug: String): User? {
        val userId = dbQuery {
            ResellerProfilesTable.selectAll()
                .where { (ResellerProfilesTable.uniqueStoreSlug eq slug) and (ResellerProfilesTable.isActive eq true) }
                .map { it[ResellerProfilesTable.userId] }.singleOrNull()
        } ?: return null

        return userRepository.findUserByEmail(
            dbQuery {
                UsersTable.selectAll().where { UsersTable.id eq userId }.map { it[UsersTable.email] }.single()
            }
        )
    }

    override suspend fun getResellerDashboard(userId: String): ResellerDashboardResponse? {
        // First, get the reseller's commission rate
        val reseller = findResellerById(userId) ?: return null
        val commissionRate = reseller.resellerProfile!!.commissionRate / 100.0

        return dbQuery {
            val allOrdersQuery = OrdersTable.selectAll()
                .where { OrdersTable.resellerId eq userId and (OrdersTable.status eq "PAID") }

            // Lifetime Stats
            val totalSalesValue = allOrdersQuery.sumOf { it[OrdersTable.total] }
            val attributedOrderCount = allOrdersQuery.count().toInt()

            // Current Month Stats
            val startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
            val salesInCurrentMonth = OrdersTable
                .selectAll()
                .where { (OrdersTable.resellerId eq userId) and (OrdersTable.status eq "PAID") and (OrdersTable.orderDate greaterEq startOfMonth) }
                .sumOf { it[OrdersTable.total] }

            // Recent Orders
            val recentOrders = OrdersTable
                .selectAll().where { OrdersTable.resellerId eq userId }
                .orderBy(OrdersTable.orderDate, SortOrder.DESC)
                .limit(5)
                .map {
                    val orderTotal = it[OrdersTable.total]
                    SimpleOrderSummary(
                        orderId = it[OrdersTable.id],
                        orderDate = it[OrdersTable.orderDate],
                        orderTotal = orderTotal,
                        commissionEarned = if (it[OrdersTable.status] == "PAID") orderTotal * commissionRate else 0.0,
                        status = it[OrdersTable.status]
                    )
                }

            ResellerDashboardResponse(
                totalSalesValue = totalSalesValue,
                totalCommissionEarned = totalSalesValue * commissionRate,
                attributedOrderCount = attributedOrderCount,
                salesInCurrentMonth = salesInCurrentMonth,
                commissionInCurrentMonth = salesInCurrentMonth * commissionRate,
                recentOrders = recentOrders
            )
        }
    }

    override suspend fun getCustomersForReseller(resellerId: String): List<ResellerCustomer> = dbQuery {
        val totalSpentAlias = OrdersTable.total.sum()
        val firstPurchaseAlias = OrdersTable.orderDate.min()

        // CORRECTED JOIN and QUERY SYNTAX
        (OrdersTable innerJoin UsersTable)
            .slice(
                UsersTable.id,
                UsersTable.firstName,
                UsersTable.lastName,
                UsersTable.email,
                totalSpentAlias,
                firstPurchaseAlias
            )
            .selectAll()
            .where { (OrdersTable.resellerId eq resellerId) and (OrdersTable.status eq "PAID") }
            .groupBy(UsersTable.id)
            .orderBy(firstPurchaseAlias, SortOrder.DESC)
            .map {
                ResellerCustomer(
                    customerId = it[UsersTable.id],
                    customerName = "${it[UsersTable.firstName]} ${it[UsersTable.lastName]}",
                    customerEmail = it[UsersTable.email],
                    firstPurchaseDate = it[firstPurchaseAlias] ?: 0L,
                    totalSpent = it[totalSpentAlias] ?: 0.0
                )
            }
    }
}