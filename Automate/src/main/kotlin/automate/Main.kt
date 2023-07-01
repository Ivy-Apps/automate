package automate

import automate.demo.article.ArticlePrinter
import automate.demo.article.statemachine.ArticleStateMachine
import automate.di.DaggerAppComponent
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

val logger: KLogger = KotlinLogging.logger {

}

suspend fun main(args: Array<String>) {
    val appComponent = DaggerAppComponent.create()
    val app = appComponent.automateApp()
    app.run()
}

class AutomateApp @Inject constructor(
    private val articleStateMachine: ArticleStateMachine,
    private val articlePrinter: ArticlePrinter
) {
    private val scope = CoroutineScope(CoroutineName("AutomateApp"))

    private var iteration = 0

    suspend fun run() {
        scope.launch {
            withContext(Dispatchers.IO) {
                articleStateMachine.state.collectLatest { state ->
                    println("Iteration #${++iteration}: -----------------------")
                    println(articlePrinter.asString(state.data))
                    println("---------------------------------------------------")
                }
            }
        }
        articleStateMachine.run()
        println("Result: ---------------------")
        val article = articleStateMachine.state.value.data
        val articleString = articlePrinter.asString(article)
        println(articleString)
        println("--------------------------")
    }
}