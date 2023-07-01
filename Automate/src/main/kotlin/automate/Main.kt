package automate

import automate.di.DaggerAppComponent
import automate.network.KtorClient
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.request.*
import javax.inject.Inject

val logger: KLogger = KotlinLogging.logger {

}

suspend fun main(args: Array<String>) {
    println("Hello World!")

    val appComponent = DaggerAppComponent.create()
    val app = appComponent.automateApp()
    app.run()
}

class AutomateApp @Inject constructor(
    private val ktorClient: KtorClient
) {
    suspend fun run() {
        println("Started...")
        println("Request 1: ${ktorClient.execute { get("https://dagger.dev/dev-guide/") }}")
        println("Request 2: ${ktorClient.execute { get("https://github.com/square/anvil") }}")
        println("Finished.")
    }
}