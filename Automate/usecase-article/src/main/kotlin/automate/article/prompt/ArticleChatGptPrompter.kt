package automate.article.prompt

import automate.Constants
import automate.article.data.Article
import automate.article.statemachine.AddSectionTransition
import automate.article.statemachine.ArticleState
import automate.article.statemachine.ArticleTransition
import automate.article.statemachine.SetTitleTransition
import automate.data.ModelFeedback
import automate.di.AppScope
import automate.di.SingleIn
import automate.normalizePrompt
import automate.openai.chatgpt.ChatGptPrompter
import automate.openai.chatgpt.ChatGptService
import automate.openai.chatgpt.data.ChatGptReply
import automate.openai.chatgpt.data.Choice
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
        val state = StateGptOptimized(
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
        )
        val response = ChatGptReply(
            choiceId = 1,
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
        val result = StateGptOptimized(
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
        )
        return Json.encodeToString(result)
    }

    override fun refineData(state: ArticleState): Article {
        return state.data
    }

    @Serializable
    data class StateGptOptimized(
        val choices: List<Choice>,
        val expectedOutcome: String,
        val feedback: List<String>,
        val article: ArticleGptOptimized,
    )
}