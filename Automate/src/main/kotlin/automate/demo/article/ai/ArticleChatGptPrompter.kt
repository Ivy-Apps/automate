package automate.demo.article.ai

import automate.ai.chatgpt.ChatGptPrompter
import automate.demo.article.Article
import automate.demo.article.statemachine.ArticleState
import automate.demo.article.statemachine.ArticleTransition
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ArticleChatGptPrompter @Inject constructor(
) : ChatGptPrompter<Article, ArticleState, ArticleTransition>() {
    override fun goal(data: Article): String {
        return """
            Write a short article on the following topic: "${data.topic}"
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