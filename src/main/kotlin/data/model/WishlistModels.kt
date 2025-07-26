package data.model

import com.example.data.model.Product
import kotlinx.serialization.Serializable

/**
 * Represents a single item in a user's wishlist, as stored in the database.
 * This is a simple, normalized model.
 */
@Serializable
data class WishlistItem(
    val userId: String,
    val productId: String,
    val dateAdded: Long // UNIX Timestamp
)

/**
 * Represents the user's complete wishlist that will be sent via the API.
 * It's a more user-friendly model because it contains the full product details,
 * preventing the client from having to make extra API calls.
 */
@Serializable
data class WishlistResponse(
    val product: Product, // The full product object
    val dateAdded: Long   // The date the item was added
)

/**
 * Represents the request body for adding an item to the wishlist.
 * Simple and specific to the task.
 */
@Serializable
data class AddToWishlistRequest(
    val productId: String
)