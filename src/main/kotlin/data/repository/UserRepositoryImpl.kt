package com.example.data.repository

import com.example.data.model.*
import com.example.plugins.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.mindrot.jbcrypt.BCrypt // <-- Importamos la librería de hashing
import java.util.UUID

class UserRepositoryImpl : UserRepository {
    private fun resultRowToUser(row: ResultRow) = User(
        id = row[UsersTable.id],
        email = row[UsersTable.email],
        name = row[UsersTable.name],
        role = row[UsersTable.role]
    )

    override suspend fun registerUser(request: RegisterRequest): User? {
        // No podemos registrar un email que ya existe
        if (findUserByEmail(request.email) != null) {
            return null
        }

        return dbQuery {
            val insertStatement = UsersTable.insert {
                it[id] = UUID.randomUUID().toString()
                it[email] = request.email
                // Hasheamos la contraseña antes de guardarla
                it[passwordHash] = BCrypt.hashpw(request.password, BCrypt.gensalt())
                it[name] = request.name
                it[role] = "CLIENTE" // Rol por defecto
            }
            resultRowToUser(insertStatement.resultedValues!!.first())
        }
    }

    override suspend fun findUserByEmail(email: String): User? = dbQuery {
        UsersTable
            .selectAll().where { UsersTable.email eq email }
            .map(::resultRowToUser)
            .singleOrNull()
    }

    // Este método busca al usuario y su hash, y comprueba si la contraseña coincide
    override suspend fun checkPassword(email: String, passwordToCheck: String): Boolean {
        val passwordHash = dbQuery {
            UsersTable
                .selectAll().where { UsersTable.email eq email }
                .map { it[UsersTable.passwordHash] }
                .singleOrNull()
        } ?: return false // Si no hay hash (usuario no existe), la contraseña es incorrecta

        return BCrypt.checkpw(passwordToCheck, passwordHash)
    }
}