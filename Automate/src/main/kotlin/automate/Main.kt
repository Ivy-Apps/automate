package automate

import automate.di.DaggerAppComponent
import automate.domain.article.ArticleProducer
import automate.domain.article.ArticleStateMachine
import automate.domain.article.data.BodyItem
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
                    val article = state.data
                    val body = article.body
                    val sections = body.count { it is BodyItem.Paragraph }
                    val images = body.count { it is BodyItem.Image }
                    val articleWords = body.sumOf {
                        when (it) {
                            is BodyItem.Image -> 0
                            is BodyItem.Paragraph -> it.text.split(" ").size
                        }
                    }
                    logger.debug(
                        """
                            
                        ------------------------------------------------------
                        Iteration #${iteration++}: 
                        Title: "${article.title}"
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