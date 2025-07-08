package com.example.data.repository

import com.example.data.model.CartItemsTable
import com.example.data.model.CartItem
import com.example.data.model.ShoppingCart
import com.example.plugins.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll

class CartRepositoryImpl : CartRepository {

    override suspend fun getCart(userId: String): ShoppingCart {
        val items = dbQuery {
            CartItemsTable.selectAll().where { CartItemsTable.userId eq userId }
                .map { resultRowToCartItem(it) }
        }
        return ShoppingCart(items)
    }

    override suspend fun addToCart(userId: String, item: CartItem): ShoppingCart {
        dbQuery {
            // Buscamos si el ítem ya existe para este usuario
            val existingItem = CartItemsTable.selectAll()
                .where { (CartItemsTable.userId eq userId) and (CartItemsTable.productId eq item.productId) }
                .singleOrNull()

            if (existingItem == null) {
                // Si no existe, lo insertamos
                CartItemsTable.insert {
                    it[this.userId] = userId
                    it[this.productId] = item.productId
                    it[this.quantity] = item.quantity
                }
            } else {
                // Si ya existe, actualizamos la cantidad
                CartItemsTable.update({
                    (CartItemsTable.userId eq userId) and (CartItemsTable.productId eq item.productId)
                }) {
                    with(SqlExpressionBuilder) {
                        it.update(quantity, quantity + item.quantity)
                    }
                }
            }
        }
        return getCart(userId)
    }

    override suspend fun removeFromCart(userId: String, productId: String): ShoppingCart {
        dbQuery {
            CartItemsTable.deleteWhere {
                (CartItemsTable.userId eq userId) and (CartItemsTable.productId eq productId)
            }
        }
        return getCart(userId)
    }

    override suspend fun clearCart(userId: String): Boolean {
        return dbQuery {
            CartItemsTable.deleteWhere { CartItemsTable.userId eq userId } > 0
        }
    }

    private fun resultRowToCartItem(row: ResultRow) = CartItem(
        productId = row[CartItemsTable.productId],
        quantity = row[CartItemsTable.quantity]
    )
}