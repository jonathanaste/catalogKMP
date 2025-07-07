package data.repository

import data.model.Address
import data.model.AddressRequest

interface AddressRepository {
    suspend fun getAddressesForUser(userId: String): List<Address>
    // Fetches a single address, ensuring it belongs to the specified user.
    suspend fun findAddressByIdForUser(userId: String, addressId: String): Address? // <-- NEW METHOD
    suspend fun addAddressForUser(userId: String, addressData: AddressRequest): Address
    suspend fun updateAddressForUser(userId: String, addressId: String, addressData: AddressRequest): Boolean
    suspend fun deleteAddressForUser(userId: String, addressId: String): Boolean
}