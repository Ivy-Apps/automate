package automate.article.prompt

import automate.article.ArticleConstants
import automate.article.data.Article
import automate.article.statemachine.AddSectionTransition
import automate.article.statemachine.ArticleState
import automate.article.statemachine.ArticleTransition
import automate.article.statemachine.SetTitleTransition
import automate.di.AppScope
import automate.di.SingleIn
import automate.openai.chatgpt.ChatGptApi
import automate.openai.chatgpt.ChatGptPrompter
import automate.openai.chatgpt.data.ChatGptResponse
import automate.openai.chatgpt.data.Choice
import automate.openai.normalizePrompt
import automate.statemachine.data.StateMachineError
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@SingleIn(AppScope::class)
class ArticleChatGptPrompter @Inject constructor(
    chatGptService: ChatGptApi,
    chatGptPrePrompter: ChatGptPrompter,
) : ChatGptPrompter<Article, ArticleState, ArticleTransition>(
    chatGptService = chatGptService,
    chatGptPrePrompter = chatGptPrePrompter,
) {
    override fun aLabel(): String {
        return ArticleConstants.MODEL_LABEL
    }

    override fun taskPrompt(): String {
        return """
Your task is to compose an informative and engaging article focusing on the topic: "${ArticleConstants.ARTICLE_TOPIC}".
Please adhere to the following guidelines for this assignment: 
${ArticleConstants.ARTICLE_REQUIREMENTS}
""".normalizePrompt()
    }

    override fun example(): ChatGptPrompter.Example {
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
        val response = ChatGptResponse(
            choiceId = 1,
            input = mapOf(
                SetTitleTransition.PARAM_TITLE.name to "[Android] Kotlin Flows + Ktor = Flawless HTTP requests",
            )
        )

        return ChatGptPrompter.Example(
            userPrompt = Json.encodeToString(state),
            chatGptResponse = response,
        )
    }

    override fun currentStateAsJson(
        state: ArticleState,
        data: Article,
        options: List<Choice>,
        feedback: List<StateMachineError>,
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
                            is StateMachineError.Transition -> "ERROR"
                            is StateMachineError.FatalError -> "FATAL ERROR"
                            is StateMachineError.Warning -> "Suggestion"
                        }
                    )
                    append("] ")
                    append(it.error)
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