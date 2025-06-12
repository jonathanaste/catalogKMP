package com.example.data.repository

import com.example.data.model.CategoriesTable
import com.example.data.model.Category
import com.example.data.model.CategoryRequest
import com.example.plugins.BadRequestException
import com.example.plugins.ConflictException
import com.example.plugins.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.util.Locale.getDefault

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
        val slug = category.name.toSlug()

        // VALIDACIÓN AÑADIDA: Nos aseguramos de que el nombre no resulte en un slug vacío.
        if (slug.isBlank()) {
            // Esto será un error 400 (Bad Request), mucho más descriptivo que un 500.
            throw BadRequestException("El nombre de la categoría debe contener caracteres alfanuméricos.")
        }

        val existingCategory = CategoriesTable
            .selectAll().where { CategoriesTable.id eq slug }
            .singleOrNull()

        if (existingCategory != null) {
            throw ConflictException("Una categoría con el slug '$slug' ya existe.")
        }

        val insertStatement = CategoriesTable.insert {
            it[id] = slug
            it[name] = category.name
            it[imageUrl] = category.imageUrl
        }

        // MANEJO MÁS SEGURO: Verificamos que la DB realmente devolvió la fila insertada.
        insertStatement.resultedValues?.firstOrNull()?.let {
            resultRowToCategory(it)
        } ?: throw IllegalStateException("Error al crear la categoría en la base de datos.")
        // Este IllegalStateException, si ocurre, dejará un mensaje claro en tus logs.
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

// Función para "slugificar" el texto
private fun String.toSlug(): String {
    return lowercase(getDefault())
        .replace(Regex("\\s+"), "-") // Reemplazar espacios por guiones
        .replace(Regex("[^a-z0-9-]"), "") // Eliminar caracteres no deseados
}