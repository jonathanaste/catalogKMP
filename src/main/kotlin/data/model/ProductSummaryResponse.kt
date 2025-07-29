package data.model

import com.example.data.model.Category
import kotlinx.serialization.Serializable

/**
 * A lightweight DTO for displaying products in a list or grid view.
 */
@Serializable
data class ProductSummaryResponse(
    val id: String,
    val name: String,
    val price: Double,
    val salePrice: Double?,
    val mainImageUrl: String,
    val averageRating: Double,
    val category: Category
)