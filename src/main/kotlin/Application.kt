package com.example

import com.example.plugins.DatabaseFactory
import com.example.routes.authRouting
import com.example.routes.cartRouting
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
        // Llama a la función que define todas las rutas de productos
        productRouting()
        authRouting(tokenProvider)
        categoryRouting()
        cartRouting()
        // Aquí podrías añadir otras rutas en el futuro
        // userRouting()
        // orderRouting()
    }
}
