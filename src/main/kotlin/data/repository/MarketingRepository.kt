package data.repository

import data.model.MarketingMaterial
import data.model.MarketingMaterialRequest

interface MarketingRepository {
    /**
     * Retrieves all available marketing materials.
     */
    suspend fun getAllMaterials(): List<MarketingMaterial>

    /**
     * Adds a new marketing material to the database.
     */
    suspend fun addMaterial(request: MarketingMaterialRequest): MarketingMaterial

    /**
     * Deletes a marketing material by its ID.
     */
    suspend fun deleteMaterial(id: String): Boolean
}