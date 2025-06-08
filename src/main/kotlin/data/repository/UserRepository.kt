package com.example.data.repository

import com.example.data.model.RegisterRequest
import com.example.data.model.User

interface UserRepository {
    suspend fun registerUser(request: RegisterRequest): User?
    suspend fun findUserByEmail(email: String): User?
    suspend fun checkPassword(email: String, passwordToCheck: String): Boolean
}