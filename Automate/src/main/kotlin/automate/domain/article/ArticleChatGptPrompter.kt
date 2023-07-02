package automate.domain.article

import automate.Constants
import automate.data.ModelFeedback
import automate.di.AppScope
import automate.di.SingleIn
import automate.domain.article.data.Article
import automate.domain.article.data.BodyItem
import automate.openai.chatgpt.ChatGptPrompter
import automate.openai.chatgpt.network.ChatGptService
import automate.openai.chatgpt.normalizePrompt
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@SingleIn(AppScope::class)
class ArticleChatGptPrompter @Inject constructor(
    chatGptService: ChatGptService
) : ChatGptPrompter<Article, ArticleState, ArticleTransition>(chatGptService) {
    override fun aLabel(): String {
        return "a Kotlin thought-leader writer"
    }

    override fun taskPrompt(): String {
        return """
        Write an article on the following topic: "${Constants.ARTICLE_TOPIC}".
        Requirements:
        ${Constants.ARTICLE_REQUIREMENTS}
        """.normalizePrompt()
    }

    override fun example(): Pair<String, ChatGptReply> {
        val state = ArticleCurrentState(
            state = Article(
                topic = "HTTP requests in Android using Kotlin using Kotlin Flows and Ktor.",
                introduction = "",
                title = "",
                body = listOf(),
                conclusion = "",
            ).optimizeForChatGpt(),
            choices = listOf(
                SetTitleTransition,
                AddSectionTransition,
            ).toOptions(),
            feedback = listOf(),
            choicesLeft = 13,
        )
        val response = ChatGptReply(
            choice = "A",
            input = mapOf(
                SetTitleTransition.PARAM_TITLE.name to "[Android] Kotlin Flows + Ktor = Flawless HTTP requests",
            )
        )

        return Json.encodeToString(state) to response
    }

    override fun currentStateAsJson(
        data: Article,
        options: List<Choice>,
        feedback: List<ModelFeedback>,
        choicesLeft: Int
    ): String {
        val state = ArticleCurrentState(
            state = data.optimizeForChatGpt(),
            choices = options,
            feedback = feedback,
            choicesLeft = choicesLeft
        )
        return Json.encodeToString(state)
    }

    override fun refineData(state: ArticleState): Article {
        return state.data.copy(
            body = state.data.body.takeLast(Constants.MAX_BODY_SECTIONS_HISTORY),
        )
    }

    @Serializable
    data class ArticleCurrentState(
        override val state: ArticleGptOptimized,
        override val choices: List<Choice>,
        override val feedback: List<ModelFeedback>,
        override val choicesLeft: Int
    ) : CurrentState<ArticleGptOptimized>
}

@Serializable
data class ArticleGptOptimized(
    val title: String,
    val topic: String,
    val introduction: String,
    val sections: List<String>,
    val images: List<String>,
    val lastSection: BodyItem.Section?,
)

private fun Article.optimizeForChatGpt(): ArticleGptOptimized {
    return ArticleGptOptimized(
        title = title,
        topic = topic,
        introduction = introduction,
        sections = body.mapNotNull {
            when (it) {
                is BodyItem.Image -> null
                is BodyItem.Section -> it.title
            }
        },
        lastSection = body.findLast { it is BodyItem.Section } as? BodyItem.Section,
        images = body.mapNotNull {
            when (it) {
                is BodyItem.Image -> it.prompt
                is BodyItem.Section -> null
            }
        },
    )
}