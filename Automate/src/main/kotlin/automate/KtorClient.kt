package automate

import automate.di.AppScope
import automate.di.SingleIn
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import javax.inject.Inject

@SingleIn(AppScope::class)
class KtorClient @Inject constructor() {
    @OptIn(ExperimentalSerializationApi::class)
    private val client by lazy {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    prettyPrintIndent = "  "
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 60_000 // 60s
                connectTimeoutMillis = 60_000 // 60s
            }

//            install(Logging) {
//                level = LogLevel.BODY
//            }
        }
    }

    suspend fun execute(block: suspend HttpClient.() -> HttpResponse): HttpResponse {
        return client.block()
    }
}