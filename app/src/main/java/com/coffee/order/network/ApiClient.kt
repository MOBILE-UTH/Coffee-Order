package com.coffee.order.network

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Singleton HTTP client for communicating with the Ktor server.
 */
object ApiClient {

    val client by lazy {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    prettyPrint = false
                })
            }
            engine {
                config {
                    connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                }
            }
        }
    }

    /** Build full URL from path */
    fun url(path: String): String = "${TokenManager.getServerUrl()}$path"
}

/** Add Bearer authorization header from stored token */
fun HttpRequestBuilder.withAuth() {
    TokenManager.getToken()?.let { bearerAuth(it) }
}
