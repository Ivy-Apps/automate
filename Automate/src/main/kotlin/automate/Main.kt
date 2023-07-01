package automate

import automate.demo.article.BodyItem
import automate.demo.article.statemachine.ArticleStateMachine
import automate.di.DaggerAppComponent
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import javax.inject.Inject

val logger: KLogger = KotlinLogging.logger {

}

suspend fun main(args: Array<String>) {
    val appComponent = DaggerAppComponent.create()
    val app = appComponent.automateApp()
    app.run()
}

class AutomateApp @Inject constructor(
    private val ktorClient: KtorClient,
    private val articleStateMachine: ArticleStateMachine,
) {
    suspend fun run() {
        articleStateMachine.run()
        println("-----------------")
        println("Result:")
        val data = articleStateMachine.state.value.data
        val article = buildString {
            append("# ${data.title}")
            data.body.forEach { item ->
                when (item) {
                    is BodyItem.Image -> {
                        append("IMAGE[")
                        append(item.prompt)
                        append("]")
                    }

                    is BodyItem.Section -> {
                        append("## ${item.title}")
                        append(item.text)
                        append("\n\n")
                    }
                }
            }
        }
        println(article)
        println("--------------------------")
    }
}