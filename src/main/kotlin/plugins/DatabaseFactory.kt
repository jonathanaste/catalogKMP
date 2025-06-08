package com.example.plugins

import com.example.data.model.CartItemsTable
import com.example.data.model.CategoriesTable
import com.example.data.model.OrderItemsTable
import com.example.data.model.OrdersTable
import com.example.data.model.ProductsTable
import com.example.data.model.UsersTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init(config: ApplicationConfig) {
        val driver = config.property("database.driver").getString()
        val url = config.property("database.url").getString()
        val user = config.property("database.user").getString()
        val userPassword = config.property("database.password").getString()
        val maxPoolSize = config.property("database.maxPoolSize").getString().toInt()

        val hikariConfig = HikariConfig().apply {
            driverClassName = driver
            jdbcUrl = url
            username = user
            password = userPassword
            maximumPoolSize = maxPoolSize
            validate()
        }

        val dataSource = HikariDataSource(hikariConfig)
        Database.connect(dataSource)

        // Esta transacción crea las tablas en la base de datos si no existen
        transaction {
            SchemaUtils.create(
                CategoriesTable,
                ProductsTable,
                UsersTable,
                OrdersTable,
                OrderItemsTable,
                CartItemsTable)
        }
    }

    // Función de utilidad para futuras consultas a la DB
    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}