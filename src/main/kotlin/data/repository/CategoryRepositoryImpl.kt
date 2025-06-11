package com.example.data.repository

import com.example.data.model.CategoriesTable
import com.example.data.model.Category
import com.example.data.model.CategoryRequest
import com.example.plugins.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.util.UUID

class CategoryRepositoryImpl : CategoryRepository {

    private fun resultRowToCategory(row: ResultRow) = Category(
        id = row[CategoriesTable.id],
        name = row[CategoriesTable.name],
        imageUrl = row[CategoriesTable.imageUrl]
    )

    override suspend fun getAllCategories(): List<Category> = dbQuery {
        CategoriesTable.selectAll().map(::resultRowToCategory)
    }

    override suspend fun addCategory(category: CategoryRequest): Category = dbQuery {
        val insertStatement = CategoriesTable.insert {
            it[id] = UUID.randomUUID().toString()
            it[name] = category.name
            it[imageUrl] = category.imageUrl
        }
        resultRowToCategory(insertStatement.resultedValues!!.first())
    }

    override suspend fun getCategoryById(id: String): Category? = dbQuery {
        CategoriesTable
            .selectAll().where { CategoriesTable.id eq id }
            .map(::resultRowToCategory)
            .singleOrNull()
    }

    override suspend fun updateCategory(id: String, category: Category): Boolean = dbQuery {
        CategoriesTable.update({ CategoriesTable.id eq id }) {
            it[name] = category.name
            it[imageUrl] = category.imageUrl
        } > 0
    }

    override suspend fun deleteCategory(id: String): Boolean = dbQuery {
        CategoriesTable.deleteWhere { CategoriesTable.id eq id } > 0
    }
}