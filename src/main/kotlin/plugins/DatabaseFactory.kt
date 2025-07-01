package com.example.plugins

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import kotlinx.coroutines.Dispatchers
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object DatabaseFactory {
    fun init() {
        val driver = System.getenv("KTOR_DATABASE_DRIVER")
            ?: throw RuntimeException("Missing environment variable KTOR_DATABASE_DRIVER")
        val url = System.getenv("KTOR_DATABASE_URL")
            ?: throw RuntimeException("Missing environment variable KTOR_DATABASE_URL")
        val user = System.getenv("KTOR_DATABASE_USER")
            ?: throw RuntimeException("Missing environment variable KTOR_DATABASE_USER")
        val userPassword = System.getenv("KTOR_DATABASE_PASSWORD")
            ?: throw RuntimeException("Missing environment variable KTOR_DATABASE_PASSWORD")
        val maxPoolSize = System.getenv("KTOR_DATABASE_MAX_POOL_SIZE")?.toIntOrNull()
            ?: throw RuntimeException("Missing or invalid environment variable KTOR_DATABASE_MAX_POOL_SIZE")

        val hikariConfig = HikariConfig().apply {
            driverClassName = driver
            jdbcUrl = url
            username = user
            password = userPassword
            maximumPoolSize = maxPoolSize
            validate()
        }

        val dataSource = HikariDataSource(hikariConfig)

        // --- AÑADIR LÓGICA DE FLYWAY ---
        // 1. Configura Flyway para que use nuestra fuente de datos
        val flyway = Flyway.configure()
            .dataSource(dataSource)
            .load()

        // 2. Ejecuta las migraciones. Flyway sabrá cuáles aplicar.
        flyway.migrate()
        // --- FIN DE LA LÓGICA DE FLYWAY ---

        Database.connect(dataSource)

    }

    // Función de utilidad para futuras consultas a la DB
    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}