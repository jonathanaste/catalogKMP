package com.example.data.repository

import com.example.data.model.RegisterRequest
import com.example.data.model.ResellerProfilesTable
import com.example.data.model.User
import com.example.data.model.UsersTable
import com.example.plugins.DatabaseFactory.dbQuery
import data.model.ResellerProfile
import data.model.UserProfileUpdateRequest
import data.repository.AddressRepository
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.mindrot.jbcrypt.BCrypt
import java.util.*

class UserRepositoryImpl(private val addressRepository: AddressRepository) : UserRepository {

    private fun resultRowToUser(row: ResultRow): User {
        // --- NEW: Check for and build the ResellerProfile ---
        val resellerProfile = if (row.hasValue(ResellerProfilesTable.userId)) {
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
            resellerProfile = resellerProfile // Assign the constructed profile
        )
    }

    override suspend fun registerUser(request: RegisterRequest): User? {
        // This logic remains unchanged for now, as it only registers CLIENTs.
        // We will later add a separate admin flow to create resellers.
        if (findUserByEmail(request.email) != null) {
            return null
        }
        return dbQuery {
            val insertStatement = UsersTable.insert {
                it[id] = UUID.randomUUID().toString()
                it[email] = request.email
                it[passwordHash] = BCrypt.hashpw(request.password, BCrypt.gensalt())
                it[firstName] = request.firstName
                it[lastName] = request.lastName
                it[phone] = null
                it[role] = "CLIENT" // New users are clients by default
            }
            resultRowToUser(insertStatement.resultedValues!!.first())
        }
    }

    override suspend fun findUserByEmail(email: String): User? {
        val user = dbQuery {
            // --- MODIFIED QUERY with LEFT JOIN ---
            (UsersTable leftJoin ResellerProfilesTable)
                .selectAll().where { UsersTable.email eq email }
                .map(::resultRowToUser)
                .singleOrNull()
        }
        // If user is found, fetch their addresses and attach them to the object
        return user?.copy(addresses = addressRepository.getAddressesForUser(user.id))
    }

    override suspend fun checkPassword(email: String, passwordToCheck: String): Boolean {
        val passwordHash = dbQuery {
            UsersTable
                .selectAll().where { UsersTable.email eq email }
                .map { it[UsersTable.passwordHash] }
                .singleOrNull()
        } ?: return false

        return BCrypt.checkpw(passwordToCheck, passwordHash)
    }

    override suspend fun updateUserProfile(userId: String, request: UserProfileUpdateRequest): Boolean = dbQuery {
        UsersTable.update({ UsersTable.id eq userId }) {
            it[firstName] = request.firstName
            it[lastName] = request.lastName
            it[phone] = request.phone
        } > 0
    }
}