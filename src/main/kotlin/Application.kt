package com.example

import com.example.di.appModule
import com.example.plugins.DatabaseFactory
import com.example.routes.authRouting
import com.example.routes.cartRouting
import com.example.routes.orderRouting
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.swagger.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import routes.categoryRouting
import routes.productRouting
import security.TokenProvider
import com.example.plugins.configureStatusPages
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.plugins.cors.routing.*
import routes.reviewRouting
import routes.supplierRouting
import routes.userRouting

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {

    install(Koin) {
        slf4jLogger() // Logger para ver qué está haciendo Koin
        modules(appModule) // Le pasamos nuestro módulo de recetas
    }

    install(CORS) {
        // Aquí va tu configuración de CORS
        // Ejemplo 1: Permitir cualquier origen (solo para desarrollo/pruebas iniciales)
        // No recomendado para producción.
        anyHost()

        // Ejemplo 2: Permitir orígenes específicos (¡Recomendado para producción!)
        // Si tu web está en http://localhost:8080 durante el desarrollo, lo agregas aquí.
        // Si tu web está en producción en https://midominio.com, también lo agregas.
        // host("http://localhost:8080")
        // host("https://midominio.com", schemes = listOf("https"))
        // host("https://www.midominio.com", schemes = listOf("https"))

        // Métodos HTTP permitidos para tus APIs
        allowMethod(HttpMethod.Options) // Siempre necesario para pre-vuelos CORS
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get) // Asumo que Get también lo vas a usar

        // Headers permitidos en las solicitudes.
        // Si tu frontend envía headers personalizados (ej. Authorization, Content-Type, Accept),
        // deben estar aquí. `Content-Type` y `Accept` son comunes.
        allowHeaders { true } // Permite todos los headers (conveniente para desarrollo)
        // O sé más específico (mejor para producción):
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization) // Necesario para tu token JWT
        // allowHeader(HttpHeaders.Accept)
        // allowHeader("X-Custom-Header") // Si usas algún header personalizado

        // Credenciales (cookies, tokens de autorización).
        // Si tu frontend envía credenciales (ej. cookies o el header Authorization),
        // tu backend debe permitirlo y tu frontend debe configurarlo (withCredentials = true en fetch/axios).
        allowCredentials = true

        // Caché del pre-vuelo CORS por este tiempo (en segundos).
        // Mejora el rendimiento al evitar repetidos pre-vuelos OPTIONS.
        maxAgeInSeconds = 3600
    }


    DatabaseFactory.init(environment.config)

    // Creamos una única instancia del proveedor de tokens
    val tokenProvider = TokenProvider(environment.config)
    configureSerialization()
    configureStatusPages()
    configureSecurity()

    routing {
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")

        productRouting()
        authRouting(tokenProvider)
        categoryRouting()
        cartRouting()
        orderRouting()
        supplierRouting()
        userRouting()
        reviewRouting()
    }
}
