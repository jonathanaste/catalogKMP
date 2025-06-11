package com.example.data.repository
import com.example.data.model.CreateSupplierRequest
import com.example.data.model.Supplier

interface SupplierRepository {
    suspend fun getAllSuppliers(): List<Supplier>
    suspend fun getSupplierById(id: String): Supplier?
    suspend fun addSupplier(supplierData: CreateSupplierRequest): Supplier
    suspend fun updateSupplier(id: String, supplierData: Supplier): Boolean
    suspend fun deleteSupplier(id: String): Boolean
}