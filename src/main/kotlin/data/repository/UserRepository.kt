package com.example.data.repository

import com.example.data.model.RegisterRequest
import com.example.data.model.User
import data.model.UserProfileUpdateRequest

interface UserRepository {
    suspend fun registerUser(request: RegisterRequest): User?
    suspend fun findUserByEmail(email: String): User?
    suspend fun checkPassword(email: String, passwordToCheck: String): Boolean
    suspend fun updateUserProfile(userId: String, request: UserProfileUpdateRequest): Boolean // <-- NEW METHOD
}