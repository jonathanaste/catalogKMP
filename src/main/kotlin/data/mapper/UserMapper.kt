package data.mapper

import com.example.data.model.*
import data.model.ResellerProfile
import org.jetbrains.exposed.sql.ResultRow

object UserMapper {
    fun resultRowToUser(row: ResultRow): User {
        val resellerProfile = if (row.getOrNull(ResellerProfilesTable.userId) != null) {
            ResellerProfile(
                userId = row[ResellerProfilesTable.userId],
                uniqueStoreSlug = row[ResellerProfilesTable.uniqueStoreSlug],
                commissionRate = row[ResellerProfilesTable.commissionRate],
                isActive = row[ResellerProfilesTable.isActive]
            )
        } else {
            null
        }

        return User(
            id = row[UsersTable.id],
            email = row[UsersTable.email],
            firstName = row[UsersTable.firstName],
            lastName = row[UsersTable.lastName],
            phone = row[UsersTable.phone],
            role = row[UsersTable.role],
            resellerProfile = resellerProfile,
            addresses = emptyList()
        )
    }
}