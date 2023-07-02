package automate.demo.article

import automate.Constants
import automate.demo.article.data.Article
import automate.di.AppScope
import automate.di.SingleIn
import automate.openai.chatgpt.ChatGptPrompter
import automate.openai.chatgpt.network.ChatGptService
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
            Write an article on the following topic "${Constants.ARTICLE_TOPIC}".
            Requirements:
            1. It must have at least 2 images.
            2. Use Markdown formatting where possible.
            3. It must NOT have repeated article sections.
            
            Bonus:
            - Keep it short.
            - Make it engaging.
            - Make it fun.
        """.trimIndent()
    }

    override fun example(): Pair<CurrentState<Article>, ChatGptReply> {
        val state = CurrentState(
            currentState = Article(
                topic = "HTTP requests in Kotlin and",
                title = "[Android/Multiplatform] Kotlin Flows + Ktor = Flawless HTTP requests",
                body = listOf()
            ),
            options = listOf(
                AddImageTransition,
                AddSectionTransition,
            ).toOptions(),
            feedback = listOf(),
            choicesLeft = 13,
        )
        val response = ChatGptReply(
            option = "B",
            input = mapOf(
                AddSectionTransition.PARAM_TITLE.name to "Introduction",
                AddSectionTransition.PARAM_TEXT.name to """
                    Let’s face it! The majority of our work is making requests to an [API]() (usually an [HTTP]() or a [GraphQL]() one) and providing a UI for all possible states — like Loading, Success, Error, and Empty.

                    This should be **simple!** But… Have we set the time to think more deeply about it? From my 8+ years as an Android Developer, more often than not, we tend to complicate things that shouldn’t be complicated at all.
                """.trimIndent()
            )
        )

        return state to response
    }
}