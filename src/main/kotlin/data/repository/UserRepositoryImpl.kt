package com.example.data.repository

import com.example.data.model.*
import com.example.plugins.DatabaseFactory.dbQuery
import data.model.UserProfileUpdateRequest
import data.repository.AddressRepository
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.mindrot.jbcrypt.BCrypt
import java.util.UUID

class UserRepositoryImpl(private val addressRepository: AddressRepository) : UserRepository {

    private fun resultRowToUser(row: ResultRow) = User(
        id = row[UsersTable.id],
        email = row[UsersTable.email],
        firstName = row[UsersTable.firstName],
        lastName = row[UsersTable.lastName],
        phone = row[UsersTable.phone],
        role = row[UsersTable.role]
        // The address list is now loaded separately.
    )

    override suspend fun registerUser(request: RegisterRequest): User? {
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
                it[role] = "CLIENT"
            }
            resultRowToUser(insertStatement.resultedValues!!.first())
        }
    }

    // This method now fetches the user AND their addresses.
    override suspend fun findUserByEmail(email: String): User? {
        val user = dbQuery {
            UsersTable
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
        } ?: return false // If no hash is found (user doesn't exist), the password is incorrect.

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