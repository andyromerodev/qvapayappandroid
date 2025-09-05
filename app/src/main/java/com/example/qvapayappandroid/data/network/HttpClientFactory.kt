package com.example.qvapayappandroid.data.network

import com.example.qvapayappandroid.BuildConfig
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.headers
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object HttpClientFactory {
    
    fun create(): HttpClient {
        return HttpClient(Android) {
            // JSON serialization
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = true
                })
            }

            // Logging for debugging
            install(Logging) {
                level = if (BuildConfig.ENABLE_LOGS) LogLevel.ALL else LogLevel.NONE
            }
            
            // Timeout configuration
            install(HttpTimeout) {
                requestTimeoutMillis = 30_000
                connectTimeoutMillis = 10_000
                socketTimeoutMillis = 30_000
            }
            
            // Default headers configuration
            defaultRequest {
                headers {
                    append("Accept", "application/json")
                    append("Content-Type", "application/json")
                }
            }
        }
    }
}