package com.example

import com.example.di.appModule
import com.example.plugins.DatabaseFactory
import com.example.routes.authRouting
import com.example.routes.cartRouting
import com.example.routes.orderRouting
import io.ktor.server.application.*
import io.ktor.server.routing.routing
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import routes.categoryRouting
import routes.productRouting
import security.TokenProvider

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {

    install(Koin) {
        slf4jLogger() // Logger para ver qué está haciendo Koin
        modules(appModule) // Le pasamos nuestro módulo de recetas
    }
    DatabaseFactory.init(environment.config)

    // Creamos una única instancia del proveedor de tokens
    val tokenProvider = TokenProvider(environment.config)
    configureSerialization()
    configureSecurity()

    routing {
        productRouting()
        authRouting(tokenProvider)
        categoryRouting()
        cartRouting()
        orderRouting()
    }
}
