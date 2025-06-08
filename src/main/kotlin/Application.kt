package com.example

import com.example.plugins.DatabaseFactory
import com.example.routes.authRouting
import com.example.routes.cartRouting
import com.example.routes.orderRouting
import io.ktor.server.application.*
import io.ktor.server.routing.routing
import routes.categoryRouting
import routes.productRouting
import security.TokenProvider

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
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
