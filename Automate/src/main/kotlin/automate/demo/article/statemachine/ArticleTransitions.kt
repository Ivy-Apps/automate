package automate.demo.article.statemachine

import arrow.core.Either
import arrow.core.raise.either
import automate.demo.article.Article
import automate.demo.article.BodyItem
import automate.statemachine.InputMap
import automate.statemachine.Transition
import automate.statemachine.TransitionParam

sealed class ArticleTransition : Transition<ArticleState, Article>()

object SetTitleTransition : ArticleTransition() {
    override val name = "Set the article title"

    private val PARAM_TITLE = TransitionParam(name = "articleTitle", type = String::class)

    override val input = listOf(PARAM_TITLE)

    override fun transition(
        state: ArticleState,
        input: InputMap
    ): Either<String, ArticleState> = either {
        val title = requiredParam(input, PARAM_TITLE)
        ArticleState.Writing(
            data = state.data.copy(title = title)
        )
    }
}

object AddSectionTransition : ArticleTransition() {
    override val name: String = "Add a paragraph section"

    private val PARAM_TITLE = TransitionParam(name = "sectionTitle", type = String::class)
    private val PARAM_TEXT = TransitionParam(
        name = "sectionText",
        type = String::class,
        description = "Supports Markdown"
    )

    override val input = listOf(PARAM_TITLE, PARAM_TEXT)

    override fun transition(
        state: ArticleState,
        input: InputMap
    ): Either<String, ArticleState> = either {
        val title = requiredParam(input, PARAM_TITLE)
        val text = requiredParam(input, PARAM_TEXT)
        val article = state.data
        val section = BodyItem.Section(title = title, text = text)
        ArticleState.Writing(
            data = article.copy(
                body = article.body + section
            )
        )
    }
}

object AddImageTransition : ArticleTransition() {
    override val name: String = "Add an image"

    private val PARAM_IMAGE_PROMPT = TransitionParam(
        name = "dallEPrompt",
        type = String::class,
        description = """
            A prompt that Dall-E will use to generate an image. The image must be fun!
        """.trimIndent()
    )

    override val input = listOf(PARAM_IMAGE_PROMPT)

    override fun transition(
        state: ArticleState,
        input: InputMap
    ): Either<String, ArticleState> = either {
        val imagePrompt = requiredParam(input, PARAM_IMAGE_PROMPT)
        val article = state.data
        ArticleState.AddedImage(
            data = article.copy(
                body = article.body + BodyItem.Image(prompt = imagePrompt)
            )
        )
    }
}

object FinalizeTransition : ArticleTransition() {
    override val name: String = "Finalize the article"

    override val input: List<TransitionParam<*>> = emptyList()

    override fun transition(state: ArticleState, input: InputMap): Either<String, ArticleState> {
        return Either.Right(
            ArticleState.Finished(state.data)
        )
    }
}