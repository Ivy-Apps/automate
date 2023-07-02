package automate

import automate.demo.article.ArticleProducer
import automate.demo.article.ArticleStateMachine
import automate.demo.article.data.BodyItem
import automate.di.DaggerAppComponent
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

val logger: KLogger = KotlinLogging.logger {}

suspend fun main(args: Array<String>) {
    val appComponent = DaggerAppComponent.create()
    val app = appComponent.automateApp()
    app.run()
}

class AutomateApp @Inject constructor(
    private val articleStateMachine: ArticleStateMachine,
    private val articleProducer: ArticleProducer
) {
    private val scope = CoroutineScope(CoroutineName("AutomateApp"))

    private var iteration = 0

    suspend fun run() {
        scope.launch {
            withContext(Dispatchers.IO) {
                articleStateMachine.state.collectLatest { state ->
                    val body = state.data.body
                    val sections = body.count { it is BodyItem.Section }
                    val images = body.count { it is BodyItem.Image }
                    val articleWords = body.sumOf {
                        when (it) {
                            is BodyItem.Image -> 0
                            is BodyItem.Section -> it.text.split(" ").size
                        }
                    }
                    logger.debug(
                        """
                            
                        ------------------------------------------------------
                        Iteration #${++iteration}: 
                        ${articleStateMachine.errors} errors | $sections sections | $images images
                        Article length: $articleWords words.
                    """.trimIndent()
                    )
                }
            }
        }
        articleStateMachine.run()
        println("Result: ---------------------")
        val article = articleStateMachine.state.value.data
        println(articleProducer.toMarkdown(article))
        println("--------------------------")

        articleProducer.saveInFile(article)
        println("Feedback:")
        println(articleStateMachine.feedback)
    }
}