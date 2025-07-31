package com.example.data.repository

import com.example.data.model.RegisterRequest
import com.example.data.model.ResellerProfilesTable
import com.example.data.model.User
import com.example.data.model.UsersTable
import com.example.plugins.DatabaseFactory.dbQuery
import data.mapper.UserMapper
import data.model.UserProfileUpdateRequest
import data.repository.AddressRepository
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.mindrot.jbcrypt.BCrypt
import java.util.*

class UserRepositoryImpl(private val addressRepository: AddressRepository) : UserRepository {

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
            // CORRECTED: Construct the User object directly and consistently.
            // Since a new user is a CLIENT, their resellerProfile is null.
            User(
                id = insertStatement[UsersTable.id],
                email = insertStatement[UsersTable.email],
                firstName = insertStatement[UsersTable.firstName],
                lastName = insertStatement[UsersTable.lastName],
                phone = insertStatement[UsersTable.phone],
                role = insertStatement[UsersTable.role],
                resellerProfile = null, // Explicitly null for new clients
                addresses = emptyList() // New users have no addresses yet
            )
        }
    }
    override suspend fun findUserByEmail(email: String): User? {
        val user = dbQuery {
            (UsersTable leftJoin ResellerProfilesTable)
                .selectAll().where { UsersTable.email eq email }
                .map(UserMapper::resultRowToUser) // <-- USE the shared mapper
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