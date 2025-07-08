package data.repository

import com.example.data.model.*
import com.example.plugins.DatabaseFactory.dbQuery
import data.model.ProductReview
import data.model.ReviewRequest
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.selectAll
import java.util.UUID

class ReviewRepositoryImpl : ReviewRepository {

    private fun resultRowToReview(row: ResultRow): ProductReview {
        val urlsString = row.getOrNull(ProductReviewsTable.photoUrls) ?: "{}"
        val photoUrls = urlsString
            .removeSurrounding("{", "}")
            .split(',')
            .filter { it.isNotBlank() }

        return ProductReview(
            id = row[ProductReviewsTable.id],
            productId = row[ProductReviewsTable.productId],
            userId = row[ProductReviewsTable.userId],
            userName = row[ProductReviewsTable.userName],
            rating = row[ProductReviewsTable.rating],
            title = row[ProductReviewsTable.title],
            comment = row[ProductReviewsTable.comment],
            photoUrls = photoUrls,
            date = row[ProductReviewsTable.date]
        )
    }

    override suspend fun getReviewsForProduct(productId: String): List<ProductReview> = dbQuery {
        ProductReviewsTable
            .selectAll().where { ProductReviewsTable.productId eq productId }
            .orderBy(ProductReviewsTable.date, SortOrder.DESC)
            .map(::resultRowToReview)
    }

    override suspend fun addReviewForProduct(productId: String, userId: String, userName: String, request: ReviewRequest): ProductReview = dbQuery {
        // This whole block is a single database transaction
        val newReviewId = UUID.randomUUID().toString()
        val currentTime = System.currentTimeMillis()

        // 1. Insert the new review
        ProductReviewsTable.insert {
            it[id] = newReviewId
            it[this.productId] = productId
            it[this.userId] = userId
            it[this.userName] = userName
            it[rating] = request.rating
            it[title] = request.title
            it[comment] = request.comment
            it[photoUrls] = "{${request.photoUrls.joinToString(",")}}"
            it[date] = currentTime
        }

        // 2. Update the product's average rating and review count
        val slice = ProductReviewsTable
            .select(ProductReviewsTable.rating.avg(), ProductReviewsTable.id.count())
        val newAverages = slice.where { ProductReviewsTable.productId eq productId }
            .first()

        val newAverageRating = newAverages[ProductReviewsTable.rating.avg()]?.toDouble() ?: 0.0
        val newReviewCount = newAverages[ProductReviewsTable.id.count()].toInt()

        ProductsTable.update({ ProductsTable.id eq productId }) {
            it[averageRating] = newAverageRating
            it[reviewCount] = newReviewCount
        }

        // 3. Return the created review object
        ProductReview(
            id = newReviewId,
            productId = productId,
            userId = userId,
            userName = userName,
            rating = request.rating,
            title = request.title,
            comment = request.comment,
            photoUrls = request.photoUrls,
            date = currentTime
        )
    }
}