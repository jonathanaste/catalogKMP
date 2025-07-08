package data.repository

import com.example.data.model.AddressesTable
import com.example.plugins.DatabaseFactory.dbQuery
import data.model.Address
import data.model.AddressRequest
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import java.util.UUID

class AddressRepositoryImpl : AddressRepository {

    private fun resultRowToAddress(row: ResultRow) = Address(
        id = row[AddressesTable.id],
        alias = row[AddressesTable.alias],
        street = row[AddressesTable.street],
        number = row[AddressesTable.number],
        postalCode = row[AddressesTable.postalCode],
        city = row[AddressesTable.city],
        state = row[AddressesTable.state],
        isDefault = row[AddressesTable.isDefault]
    )

    override suspend fun getAddressesForUser(userId: String): List<Address> = dbQuery {
        AddressesTable.selectAll().where { AddressesTable.userId eq userId }
            .map(::resultRowToAddress)
    }

    override suspend fun findAddressByIdForUser(userId: String, addressId: String): Address? = dbQuery {
        AddressesTable
            .selectAll().where { (AddressesTable.id eq addressId) and (AddressesTable.userId eq userId) }
            .map(::resultRowToAddress)
            .singleOrNull()
    }

    override suspend fun addAddressForUser(userId: String, addressData: AddressRequest): Address {
        val newId = UUID.randomUUID().toString()
        return dbQuery {
            val insertStatement = AddressesTable.insert {
                it[id] = newId
                it[this.userId] = userId
                it[alias] = addressData.alias
                it[street] = addressData.street
                it[number] = addressData.number
                it[postalCode] = addressData.postalCode
                it[city] = addressData.city
                it[state] = addressData.state
                it[isDefault] = addressData.isDefault
            }
            resultRowToAddress(insertStatement.resultedValues!!.first())
        }
    }

    override suspend fun updateAddressForUser(userId: String, addressId: String, addressData: AddressRequest): Boolean = dbQuery {
        AddressesTable.update({ (AddressesTable.id eq addressId) and (AddressesTable.userId eq userId) }) {
            it[alias] = addressData.alias
            it[street] = addressData.street
            it[number] = addressData.number
            it[postalCode] = addressData.postalCode
            it[city] = addressData.city
            it[state] = addressData.state
            it[isDefault] = addressData.isDefault
        } > 0
    }

    override suspend fun deleteAddressForUser(userId: String, addressId: String): Boolean = dbQuery {
        // CORRECTED: Explicitly reference the table columns instead of using the implicit 'it'.
        AddressesTable.deleteWhere { (AddressesTable.id eq addressId) and (AddressesTable.userId eq userId) } > 0
    }
}