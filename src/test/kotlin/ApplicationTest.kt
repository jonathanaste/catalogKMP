package com.example

import com.example.data.model.LoginRequest
import com.example.data.model.RegisterRequest
import com.example.data.model.User
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ApplicationTest {

    @Test
    fun `Test de registro y login de usuario`() = testApplication {
        // testApplication levanta un servidor de prueba en memoria.

        // Creamos un cliente HTTP para el test que entiende JSON.
        val client = createClient {
            this@testApplication.install(ContentNegotiation) {
                json(Json { isLenient = true; ignoreUnknownKeys = true })
            }
        }

        // --- FASE 1: REGISTRO ---

        // Datos para el nuevo usuario
        val newUser = RegisterRequest(
            email = "test.user.${System.currentTimeMillis()}@example.com", // Email único para cada test
            password = "password123",
            name = "Test User"
        )

        // Hacemos la petición POST a /auth/register
        val registerResponse = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(newUser)
        }

        // Verificamos que la respuesta sea 201 Created
        assertEquals(HttpStatusCode.Created, registerResponse.status)

        // Verificamos que el cuerpo de la respuesta contenga un usuario con el email correcto
        val registeredUser = registerResponse.body<User>()
        assertEquals(newUser.email, registeredUser.email)

        // --- FASE 2: LOGIN ---

        val loginCredentials = LoginRequest(
            email = newUser.email,
            password = newUser.password
        )

        // Hacemos la petición POST a /auth/login
        val loginResponse = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(loginCredentials)
        }

        // Verificamos que la respuesta sea 200 OK
        assertEquals(HttpStatusCode.OK, loginResponse.status)

        // Verificamos que la respuesta contenga un token
        val loginBody = loginResponse.body<String>()
        val jsonBody = Json.parseToJsonElement(loginBody).jsonObject
        val token = jsonBody["token"]?.jsonPrimitive?.content

        assertNotNull(token, "El token no debería ser nulo")
    }
}