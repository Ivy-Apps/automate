package automate.demo.article.ai

import automate.demo.article.Article
import automate.demo.article.statemachine.ArticleState
import automate.demo.article.statemachine.ArticleTransition
import automate.di.AppScope
import automate.di.SingleIn
import automate.openai.chatgpt.ChatGptPrompter
import automate.openai.chatgpt.ChatGptService
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@SingleIn(AppScope::class)
class ArticleChatGptPrompter @Inject constructor(
    chatGptService: ChatGptService
) : ChatGptPrompter<Article, ArticleState, ArticleTransition>(chatGptService) {
    override fun goal(data: Article): String {
        return """
            Write an article on the following topic: "${data.topic}"
            Requirements:
            - It must have at least 2 images.
            - Make it engaging.
            - Use Markdown formatting where possible.
            - Keep it short.
            - You must not repeat yourself.
        """.trimIndent()
    }

    override fun currentStateJson(state: ArticleState, error: String?): String {
        return ArticleStateJson(
            state = state.data,
            error = error
        ).let {
            Json.encodeToString(it)
        }
    }
}

@Serializable
data class ArticleStateJson(
    val state: Article,
    val error: String?,
)