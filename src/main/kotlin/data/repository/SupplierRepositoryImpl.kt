package com.example.data.repository

import com.example.data.model.CreateSupplierRequest
import com.example.data.model.ProductsTable
import com.example.data.model.Supplier
import com.example.data.model.SuppliersTable
import com.example.plugins.ConflictException
import com.example.plugins.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import java.util.*

class SupplierRepositoryImpl : SupplierRepository {

    private fun resultRowToSupplier(row: ResultRow) = Supplier(
        id = row[SuppliersTable.id],
        name = row[SuppliersTable.name],
        contactPerson = row[SuppliersTable.contactPerson],
        phone = row[SuppliersTable.phone],
        email = row[SuppliersTable.email],
        cbu = row[SuppliersTable.cbu],
        aliasCbu = row[SuppliersTable.aliasCbu],
        notes = row[SuppliersTable.notes]
    )

    override suspend fun getAllSuppliers(): List<Supplier> = dbQuery {
        SuppliersTable.selectAll().map(::resultRowToSupplier)
    }

    override suspend fun getSupplierById(id: String): Supplier? = dbQuery {
        SuppliersTable
            .selectAll().where { SuppliersTable.id eq id }
            .map(::resultRowToSupplier)
            .singleOrNull()
    }

    override suspend fun addSupplier(supplierData: CreateSupplierRequest): Supplier {
        val newId = UUID.randomUUID().toString()
        return dbQuery {
            val insertStatement = SuppliersTable.insert {
                it[id] = newId
                it[name] = supplierData.name
                it[contactPerson] = supplierData.contactPerson
                it[phone] = supplierData.phone
                it[email] = supplierData.email
                it[cbu] = supplierData.cbu
                it[aliasCbu] = supplierData.aliasCbu
                it[notes] = supplierData.notes
            }
            resultRowToSupplier(insertStatement.resultedValues!!.first())
        }
    }

    override suspend fun updateSupplier(id: String, supplierData: Supplier): Boolean = dbQuery {
        SuppliersTable.update({ SuppliersTable.id eq id }) {
            it[name] = supplierData.name
            it[contactPerson] = supplierData.contactPerson
            it[phone] = supplierData.phone
            it[email] = supplierData.email
            it[cbu] = supplierData.cbu
            it[aliasCbu] = supplierData.aliasCbu
            it[notes] = supplierData.notes
        } > 0
    }

    override suspend fun deleteSupplier(id: String): Boolean = dbQuery {
        val existingProducts = ProductsTable.selectAll().where { ProductsTable.supplierId eq id }.count()

        if (existingProducts > 0) {
            // Throw a specific, catchable exception instead of letting the DB crash.
            // Our StatusPages plugin will convert this to a 409 Conflict response.
            throw ConflictException("Cannot delete supplier. Reassign or delete ${existingProducts} associated product(s) first.")
        }

        // If the check passes, proceed with the deletion
        SuppliersTable.deleteWhere { SuppliersTable.id eq id } > 0
    }
}