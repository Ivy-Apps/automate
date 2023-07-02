package automate.domain.article

import arrow.core.Either
import arrow.core.raise.either
import automate.data.ModelFeedback.Error
import automate.data.ModelFeedback.Suggestion
import automate.domain.article.data.Article
import automate.domain.article.data.BodyItem
import automate.openai.chatgpt.ChatGptPrompter
import automate.statemachine.InputMap
import automate.statemachine.Transition
import automate.statemachine.TransitionParam

sealed class ArticleTransition : Transition<ArticleState, Article>()

object SetTitleTransition : ArticleTransition() {
    override val name = "Set the article title"
    override val description = "Choose an engaging and relevant title."

    val PARAM_TITLE = TransitionParam(
        name = "title",
        description = "The title of the Article up to 100 chars",
        tips = listOf(
            "Target length: around 60 chars.",
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
        ArticleState.WriteIntroduction(
            data = state.data.copy(title = title)
        ) to emptyList()
    }
}

object WriteIntroduction : ArticleTransition() {
    override val name: String = "Write the introduction"
    override val description: String = "Hook. Grab readers attention with a few short sentences."

    private val PARAM_INTRODUCTION = TransitionParam(
        name = "introduction",
        type = String::class,
        description = """
            Prepare the reader for what they'll learn in this article in a fun and engaging way.
        """.trimIndent(),
        tips = listOf(
            "Keep it very short."
        ),
    )

    override val input: List<TransitionParam<*>> = listOf(PARAM_INTRODUCTION)

    override fun transition(
        state: ArticleState,
        input: InputMap
    ): Either<Error, Pair<ArticleState, List<Suggestion>>> = either {
        val introduction = requiredParam(input, PARAM_INTRODUCTION)
        ArticleState.WriteBody(
            data = state.data.copy(
                introduction = introduction,
            )
        ) to emptyList()
    }

}

object AddSectionTransition : ArticleTransition() {
    override val name = "Add an article section"
    override val description =
        "Represent a point in the article, each section has a title and consists of one or more paragraphs."

    val PARAM_TITLE = TransitionParam(
        name = "title",
        description = "The title of the article section.",
        type = String::class
    )
    val PARAM_TEXT = TransitionParam(
        name = "text",
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
        val title = requiredParam(input, PARAM_TITLE)
        val text = requiredParam(input, PARAM_TEXT)
        val article = state.data

        val duplicatedSection = article.body.any {
            (it as? BodyItem.Section)?.title?.equals(title, ignoreCase = true) == true
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

        val section = BodyItem.Section(title = title, text = text)
        ArticleState.WriteBody(
            data = article.copy(
                body = article.body + section
            )
        ) to emptyList()
    }
}

object AddImageTransition : ArticleTransition() {
    override val name: String = "Add an image"
    override val description = "Image prompt for an AI model so the reader isn't bored of just text."

    private val PARAM_IMAGE_PROMPT = TransitionParam(
        name = "prompt",
        type = String::class,
        description = """
            A prompt that Dall-E will use to generate an image.
        """.trimIndent(),
        tips = listOf(
            "Be creative, make it unique.",
            "Use bold or abstract prompts",
            "The image must be fun!",
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

object WriteConclusionTransition : ArticleTransition() {
    override val name: String = "${ChatGptPrompter.FINALIZE_TAG} Write a conclusion"
    override val description = "Conclude the article with a few final sentences."

    private val PARAM_CONCLUSION = TransitionParam(
        name = "conclusion",
        type = String::class,
        description = """
            Conclusion to summarize and finish the article.
        """.trimIndent(),
        tips = null,
    )

    override val input: List<TransitionParam<*>> = listOf(PARAM_CONCLUSION)


    override fun transition(
        state: ArticleState,
        input: InputMap
    ): Either<Error, Pair<ArticleState, List<Suggestion>>> = either {
        val conclusion = requiredParam(input, PARAM_CONCLUSION)
        ArticleState.Conclusion(
            state.data.copy(
                conclusion = conclusion,
            )
        ) to emptyList()
    }
}