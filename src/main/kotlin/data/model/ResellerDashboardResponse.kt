package data.model

import kotlinx.serialization.Serializable

/**
 * A summary of a reseller's most recent orders for their dashboard.
 */
@Serializable
data class SimpleOrderSummary(
    val orderId: String,
    val orderDate: Long,
    val orderTotal: Double,
    val commissionEarned: Double,
    val status: String
)

/**
 * Represents the data structure for the reseller's main dashboard.
 */
@Serializable
data class ResellerDashboardResponse(
    // Lifetime stats
    val totalSalesValue: Double,
    val totalCommissionEarned: Double,
    val attributedOrderCount: Int,

    // Current month stats
    val salesInCurrentMonth: Double,
    val commissionInCurrentMonth: Double,

    // A quick look at recent activity
    val recentOrders: List<SimpleOrderSummary>
)