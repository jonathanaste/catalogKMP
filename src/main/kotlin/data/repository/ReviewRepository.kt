package data.repository

import data.model.ProductReview
import data.model.ReviewRequest

interface ReviewRepository {
    suspend fun getReviewsForProduct(productId: String): List<ProductReview>
    suspend fun addReviewForProduct(productId: String, userId: String, userName: String, request: ReviewRequest): ProductReview
}