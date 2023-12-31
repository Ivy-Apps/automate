package automate

import automate.article.data.BodyItem
import automate.article.sectionsTitles
import automate.article.statemachine.ArticleStateMachine
import automate.article.wordsCount
import automate.domain.article.ArticleProducer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

class AutomateApp @Inject constructor(
    private val stateMachine: ArticleStateMachine,
    private val articleProducer: ArticleProducer
) {
    private val scope = CoroutineScope(CoroutineName("AutomateApp"))

    private var iteration = 0

    suspend fun run() {
        scope.launch {
            withContext(Dispatchers.IO) {
                stateMachine.state.collectLatest { state ->
                    val article = state.data
                    val body = article.body
                    val sections = body.count { it is BodyItem.Section }
                    val articleWords = article.wordsCount()
                    logger.debug(
                        """
    
------------------------------------------------------
Iteration #${iteration++}: 
Title: "${article.title}"
Introduction: ${article.introduction.wordsCount()} words.
Sections:
-${article.sectionsTitles().joinToString(separator = "\n-")}
$sections sections | ${stateMachine.activeErrors()} active errors | ${stateMachine.errorsOccurred} total errors
Article length: $articleWords words.
""".trimIndent()
                    )
                }
            }
        }
        stateMachine.run()
        println("Result: ---------------------")
        val article = stateMachine.state.value.data
        println(articleProducer.toMarkdown(article))
        println("--------------------------")

        articleProducer.saveInFile(article)
        println("Feedback:")
        println(stateMachine.feedback)
    }
}