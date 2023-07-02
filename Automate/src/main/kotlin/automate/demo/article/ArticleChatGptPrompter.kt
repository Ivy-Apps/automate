package automate.demo.article

import automate.Constants
import automate.data.ModelFeedback
import automate.demo.article.data.Article
import automate.di.AppScope
import automate.di.SingleIn
import automate.openai.chatgpt.ChatGptPrompter
import automate.openai.chatgpt.network.ChatGptService
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
        ${Constants.ARTICLE_REQUIREMENTS}
        """.trimIndent()
    }

    override fun example(): Pair<String, ChatGptReply> {
        val state = ArticleCurrentState(
            currentState = Article(
                topic = "HTTP requests in Kotlin and",
                title = "[Android/Multiplatform] Kotlin Flows + Ktor = Flawless HTTP requests",
                body = listOf()
            ),
            choices = listOf(
                AddImageTransition,
                AddParagraphTransition,
            ).toOptions(),
            feedback = listOf(),
            choicesLeft = 13,
        )
        val response = ChatGptReply(
            choice = "B",
            input = mapOf(
                AddParagraphTransition.PARAM_TITLE.name to "Introduction",
                AddParagraphTransition.PARAM_TEXT.name to """
                    Let’s face it! The majority of our work is making requests to an [API]() (usually an [HTTP]() or a [GraphQL]() one) and providing a UI for all possible states — like Loading, Success, Error, and Empty.
                    
                    This should be **simple!** But… Have we set the time to think more deeply about it? From my 8+ years as an Android Developer, more often than not, we tend to complicate things that shouldn’t be complicated at all.
                """.trimIndent()
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
            currentState = data,
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
        override val currentState: Article,
        override val choices: List<Choice>,
        override val feedback: List<ModelFeedback>,
        override val choicesLeft: Int
    ) : CurrentState<Article>
}