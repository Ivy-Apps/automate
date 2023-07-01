package automate

import automate.di.AppScope
import automate.di.SingleIn
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import javax.inject.Inject

@SingleIn(AppScope::class)
class KtorClient @Inject constructor() {
    private val client by lazy {
        logger.debug("Initializing KtorClient...")

        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }

//            install(Logging)
        }.also {
            logger.debug("KtorClient initialized.")
        }
    }

    suspend fun execute(block: suspend HttpClient.() -> HttpResponse): HttpResponse {
        return client.block()
    }
}