package automate.demo.article

import automate.data.ModelFeedback
import automate.demo.article.data.Article
import automate.di.AppScope
import automate.di.SingleIn
import automate.openai.chatgpt.ChatGptPrompter
import automate.openai.chatgpt.network.ChatGptResponse
import automate.openai.chatgpt.network.ChatGptService
import automate.statemachine.State
import kotlinx.serialization.Serializable
import javax.inject.Inject

@SingleIn(AppScope::class)
class ArticleChatGptPrompter @Inject constructor(
    chatGptService: ChatGptService
) : ChatGptPrompter<Article, ArticleState, ArticleTransition>(chatGptService) {
    override fun aLabel(data: Article): String {
        return "a Kotlin thought-leader writer"
    }

    override fun taskPrompt(data: Article): String {
        return """
            Write an article on the following topic "${data.topic}".
            Requirements:
            - It must have at least 2 images.
            - Use Markdown formatting where possible.
            - It must NOT have repeated article sections.
            
            Bonus:
            - Keep it short.
            - Make it engaging.
            - Make it fun.
        """.trimIndent()
    }

    override fun example(): Pair<State<Article>, ChatGptResponse> {
        TODO("Not yet implemented")
    }
}

@Serializable
data class ArticleStateJson(
    val state: Article,
    val feedback: List<ModelFeedback>,
)