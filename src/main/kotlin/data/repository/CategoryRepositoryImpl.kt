package com.example.data.repository

import com.example.data.model.CategoriesTable
import com.example.data.model.Category
import com.example.plugins.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class CategoryRepositoryImpl : CategoryRepository {

    private fun resultRowToCategory(row: ResultRow) = Category(
        id = row[CategoriesTable.id],
        name = row[CategoriesTable.name],
        imageUrl = row[CategoriesTable.imageUrl]
    )

    override suspend fun getAllCategories(): List<Category> = dbQuery {
        CategoriesTable.selectAll().map(::resultRowToCategory)
    }

    override suspend fun addCategory(category: Category): Category = dbQuery {
        val insertStatement = CategoriesTable.insert {
            it[id] = category.id
            it[name] = category.name
            it[imageUrl] = category.imageUrl
        }
        resultRowToCategory(insertStatement.resultedValues!!.first())
    }
}