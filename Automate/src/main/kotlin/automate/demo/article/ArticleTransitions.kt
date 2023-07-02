package automate.demo.article

import arrow.core.Either
import arrow.core.raise.either
import automate.data.ModelFeedback.Error
import automate.data.ModelFeedback.Suggestion
import automate.demo.article.data.Article
import automate.demo.article.data.BodyItem
import automate.openai.chatgpt.ChatGptPrompter
import automate.statemachine.InputMap
import automate.statemachine.Transition
import automate.statemachine.TransitionParam

sealed class ArticleTransition : Transition<ArticleState, Article>()

object SetTitleTransition : ArticleTransition() {
    override val name = "Set the article title"

    private val PARAM_TITLE = TransitionParam(
        name = "articleTitle",
        description = "The title of the Article up to 100 chars",
        tips = listOf(
            "Make it as short as possible",
            """
            Use symbols like '[]', ':', '-' for better formatting.
            """.trimIndent(),
        ),
        type = String::class,
    )

    override val input = listOf(PARAM_TITLE)

    override fun transition(
        state: ArticleState,
        input: InputMap
    ): Either<Error, Pair<ArticleState, List<Suggestion>>> = either {
        val title = requiredParam(input, PARAM_TITLE)
        ArticleState.Writing(
            data = state.data.copy(title = title)
        ) to emptyList()
    }
}

object AddParagraphTransition : ArticleTransition() {
    override val name: String = "Add a paragraph"

    val PARAM_TITLE = TransitionParam(
        name = "title",
        description = "The title of the article paragraph.",
        optional = false,
        type = String::class
    )
    val PARAM_TEXT = TransitionParam(
        name = "body",
        description = "The text of the article paragraph.",
        type = String::class,
        tips = listOf(
            "Supports Markdown.",
            "Code examples are recommended!",
            "Keep it short, straightforward and fun."
        )
    )

    override val input = listOf(PARAM_TITLE, PARAM_TEXT)

    override fun transition(
        state: ArticleState,
        input: InputMap
    ): Either<Error, Pair<ArticleState, List<Suggestion>>> = either {
        val title = optionalParamø(input, PARAM_TITLE)
        val text = requiredParam(input, PARAM_TEXT)
        val article = state.data

        val duplicatedSection = article.body.any {
            (it as? BodyItem.Paragraph)?.title?.equals(title, ignoreCase = true) == true
        }
        if (duplicatedSection) {
            raise(
                Error(
                    """
                    Error: Duplicated paragraph '$title'. Don't try to add it again!
                """.trimIndent()
                )
            )
        }

        val section = BodyItem.Paragraph(title = title, text = text)
        ArticleState.Writing(
            data = article.copy(
                body = article.body + section
            )
        ) to emptyList()
    }
}

object AddImageTransition : ArticleTransition() {
    override val name: String = "Add an image"

    private val PARAM_IMAGE_PROMPT = TransitionParam(
        name = "prompt",
        type = String::class,
        description = """
            A prompt that Dall-E will use to generate an image. The image must be fun!
        """.trimIndent(),
        tips = listOf(
            "Be creative, make it unique.",
            "Use more bold prompts",
        )
    )

    override val input = listOf(PARAM_IMAGE_PROMPT)

    override fun transition(
        state: ArticleState,
        input: InputMap
    ): Either<Error, Pair<ArticleState, List<Suggestion>>> = either {
        val imagePrompt = requiredParam(input, PARAM_IMAGE_PROMPT)
        val article = state.data
        ArticleState.AddedImage(
            data = article.copy(
                body = article.body + BodyItem.Image(prompt = imagePrompt)
            )
        ) to emptyList()
    }
}

object FinalizeTransition : ArticleTransition() {
    override val name: String = "${ChatGptPrompter.FINALIZE_TAG} Finish the article"

    override val input: List<TransitionParam<*>> = emptyList()

    override fun transition(
        state: ArticleState,
        input: InputMap
    ): Either<Error, Pair<ArticleState, List<Suggestion>>> = either {
        ArticleState.Finished(state.data) to emptyList()
    }
}