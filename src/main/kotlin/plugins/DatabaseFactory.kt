package com.example.plugins

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import kotlinx.coroutines.Dispatchers
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

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