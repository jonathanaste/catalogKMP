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
            resellerProfile = resellerProfile
        )
    }

    override suspend fun registerUser(request: RegisterRequest): User? {
        if (findUserByEmail(request.email) != null) {
            return null
        }
        val newUserEmail = dbQuery {
            val insertStatement = UsersTable.insert {
                it[id] = UUID.randomUUID().toString()
                it[email] = request.email
                it[passwordHash] = BCrypt.hashpw(request.password, BCrypt.gensalt())
                it[firstName] = request.firstName
                it[lastName] = request.lastName
                it[phone] = null
                it[role] = "CLIENT"
            }
            insertStatement[UsersTable.email]
        }
        // --- CORRECTED: Call the method directly on the class instance ---
        return findUserByEmail(newUserEmail)
    }

    override suspend fun findUserByEmail(email: String): User? {
        val user = dbQuery {
            (UsersTable leftJoin ResellerProfilesTable)
                .selectAll().where { UsersTable.email eq email }
                .map(::resultRowToUser)
                .singleOrNull()
        }
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