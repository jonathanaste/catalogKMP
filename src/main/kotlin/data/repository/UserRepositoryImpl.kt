package com.example.data.repository

import com.example.data.model.*
import com.example.plugins.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.mindrot.jbcrypt.BCrypt
import java.util.UUID

class UserRepositoryImpl : UserRepository {
    private fun resultRowToUser(row: ResultRow) = User(
        id = row[UsersTable.id],
        email = row[UsersTable.email],
        firstName = row[UsersTable.firstName], // <-- UPDATED
        lastName = row[UsersTable.lastName],   // <-- UPDATED
        phone = row[UsersTable.phone],         // <-- NEW
        role = row[UsersTable.role]
        // Note: The address list will be loaded separately when needed to avoid complex joins here.
    )

    override suspend fun registerUser(request: RegisterRequest): User? {
        if (findUserByEmail(request.email) != null) {
            return null // Email already exists.
        }

        return dbQuery {
            val insertStatement = UsersTable.insert {
                it[id] = UUID.randomUUID().toString()
                it[email] = request.email
                it[passwordHash] = BCrypt.hashpw(request.password, BCrypt.gensalt())
                it[firstName] = request.firstName // <-- UPDATED
                it[lastName] = request.lastName   // <-- UPDATED
                it[phone] = null // Phone can be added later by the user.
                it[role] = "CLIENT" // Default role upon registration.
            }
            // Map the first result from the insert statement back to a User object.
            resultRowToUser(insertStatement.resultedValues!!.first())
        }
    }

    override suspend fun findUserByEmail(email: String): User? = dbQuery {
        UsersTable
            .selectAll().where { UsersTable.email eq email }
            .map(::resultRowToUser)
            .singleOrNull()
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
}