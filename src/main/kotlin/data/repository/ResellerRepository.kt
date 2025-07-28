package data.repository

import com.example.data.model.User
import data.model.ResellerCreateRequest
import data.model.ResellerUpdateRequest

interface ResellerRepository {
    /**
     * Creates a new user with the RESELLER role and their associated profile.
     * @return The newly created User object, or null if the email or slug already exists.
     */
    suspend fun createReseller(request: ResellerCreateRequest): User?

    /**
     * Retrieves a list of all users with the RESELLER role.
     */
    suspend fun getAllResellers(): List<User>

    /**
     * Finds a single reseller by their user ID.
     * @return The User object with their reseller profile, or null if not found.
     */
    suspend fun findResellerById(userId: String): User? // <-- ADD THIS

    /**
     * Updates a reseller's profile information.
     * @return True if the update was successful, false otherwise.
     */
    suspend fun updateReseller(userId: String, request: ResellerUpdateRequest): Boolean // <-- ADD THIS

    /**
     * Deletes a user with the RESELLER role.
     * Note: This will also delete their associated profile due to DB constraints.
     * @return True if the deletion was successful, false otherwise.
     */
    suspend fun deleteReseller(userId: String): Boolean // <-- ADD THIS
}