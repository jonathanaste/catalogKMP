package data.repository

import com.example.data.model.MarketingMaterialsTable
import com.example.plugins.DatabaseFactory.dbQuery
import data.model.MarketingMaterial
import data.model.MarketingMaterialRequest
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.util.*

class MarketingRepositoryImpl : MarketingRepository {

    private fun resultRowToMarketingMaterial(row: ResultRow) = MarketingMaterial(
        id = row[MarketingMaterialsTable.id],
        title = row[MarketingMaterialsTable.title],
        description = row[MarketingMaterialsTable.description],
        assetUrl = row[MarketingMaterialsTable.assetUrl],
        assetType = row[MarketingMaterialsTable.assetType],
        dateAdded = row[MarketingMaterialsTable.dateAdded]
    )

    override suspend fun getAllMaterials(): List<MarketingMaterial> = dbQuery {
        MarketingMaterialsTable.selectAll().map(::resultRowToMarketingMaterial)
    }

    override suspend fun addMaterial(request: MarketingMaterialRequest): MarketingMaterial {
        val newId = UUID.randomUUID().toString()
        return dbQuery {
            MarketingMaterialsTable.insert {
                it[id] = newId
                it[title] = request.title
                it[description] = request.description
                it[assetUrl] = request.assetUrl
                it[assetType] = request.assetType
                it[dateAdded] = System.currentTimeMillis()
            }.resultedValues!!.first().let(::resultRowToMarketingMaterial)
        }
    }

    override suspend fun deleteMaterial(id: String): Boolean = dbQuery {
        MarketingMaterialsTable.deleteWhere { MarketingMaterialsTable.id eq id } > 0
    }
}