package data.repository

import com.example.data.model.User
import data.model.ResellerCreateRequest
import data.model.ResellerCustomer
import data.model.ResellerDashboardResponse
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
    suspend fun findResellerById(userId: String): User?

    /**
     * Updates a reseller's profile information.
     * @return True if the update was successful, false otherwise.
     */
    suspend fun updateReseller(userId: String, request: ResellerUpdateRequest): Boolean

    /**
     * Deletes a user with the RESELLER role.
     * Note: This will also delete their associated profile due to DB constraints.
     * @return True if the deletion was successful, false otherwise.
     */
    suspend fun deleteReseller(userId: String): Boolean

    /**
     * Finds an active reseller by their unique store slug.
     * @return The User object, or null if no active reseller is found with that slug.
     */
    suspend fun findActiveResellerBySlug(slug: String): User?

    /**
     * Calculates and retrieves the dashboard statistics for a given reseller.
     * @return A ResellerDashboardResponse object with the calculated data.
     */
    suspend fun getResellerDashboard(userId: String): ResellerDashboardResponse?

    /**
     * Retrieves a list of customers who have purchased through the given reseller.
     * @return A list of ResellerCustomer objects.
     */
    suspend fun getCustomersForReseller(resellerId: String): List<ResellerCustomer>
}