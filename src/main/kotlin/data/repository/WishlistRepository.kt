package data.repository

import data.model.WishlistItem
import data.model.WishlistResponse

interface WishlistRepository {
    /**
     * Retrieves all wishlist items for a specific user, returning them as rich response objects.
     * @param userId The ID of the user whose wishlist is being requested.
     * @return A list of [WishlistResponse] objects containing full product details.
     */
    suspend fun getWishlistForUser(userId: String): List<WishlistResponse>

    /**
     * Adds a product to a user's wishlist.
     * @param userId The ID of the user.
     * @param productId The ID of the product to add.
     * @return The newly created [WishlistItem] or null if it already exists.
     */
    suspend fun addToWishlist(userId: String, productId: String): WishlistItem?

    /**
     * Removes a product from a user's wishlist.
     * @param userId The ID of the user.
     * @param productId The ID of the product to remove.
     * @return True if the item was successfully removed, false otherwise.
     */
    suspend fun removeFromWishlist(userId: String, productId: String): Boolean
}