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
        return Constants.MODEL_LABEL
    }

    override fun taskPrompt(): String {
        return """
Your task is to compose an informative and engaging article focusing on the topic: "${Constants.ARTICLE_TOPIC}".
Please adhere to the following guidelines for this assignment: 
${Constants.ARTICLE_REQUIREMENTS}
""".normalizePrompt()
    }

    override fun example(): Pair<String, ChatGptReply> {
        val state = ArticleCurrentState(
            article = Article(
                topic = "HTTP requests in Android using Kotlin using Kotlin Flows and Ktor.",
                introduction = "",
                title = "",
                body = listOf(),
                conclusion = "",
            ).optimizeForChatGpt(),
            expectedOutcome = ArticleState.SetTitle.expectedOutcome,
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
        state: ArticleState,
        data: Article,
        options: List<Choice>,
        feedback: List<ModelFeedback>,
        choicesLeft: Int
    ): String {
        val result = ArticleCurrentState(
            article = data.optimizeForChatGpt(),
            expectedOutcome = state.expectedOutcome,
            choices = options,
            feedback = feedback.map {
                buildString {
                    append('[')
                    append(
                        when (it) {
                            is ModelFeedback.Error -> "ERROR"
                            is ModelFeedback.FatalError -> "FATAL ERROR"
                            is ModelFeedback.Suggestion -> "Suggestion"
                        }
                    )
                    append("] ")
                    append(it.feedback)
                }
            },
            choicesLeft = choicesLeft
        )
        return Json.encodeToString(result)
    }

    override fun refineData(state: ArticleState): Article {
        return state.data
    }

    @Serializable
    data class ArticleCurrentState(
        override val article: ArticleGptOptimized,
        override val expectedOutcome: String,
        override val choices: List<Choice>,
        override val feedback: List<String>,
        override val choicesLeft: Int
    ) : CurrentState<ArticleGptOptimized>
}

@Serializable
data class ArticleGptOptimized(
    val title: String,
    val topic: String,
    val introduction: String,
    val sectionsTitles: List<String>,
    val body: List<BodyItem>,
)

private fun Article.optimizeForChatGpt(): ArticleGptOptimized {
    return ArticleGptOptimized(
        title = title,
        topic = topic,
        introduction = introduction,
        sectionsTitles = sectionsTitles(),
        body = body,
    )
}