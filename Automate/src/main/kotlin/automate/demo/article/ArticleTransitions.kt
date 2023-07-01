package automate.demo.article

import arrow.core.Either
import arrow.core.raise.either
import automate.statemachine.InputMap
import automate.statemachine.Transition
import automate.statemachine.TransitionParam

sealed class ArticleTransition : Transition<ArticleState, Article>()

object SetTitleTransition : ArticleTransition() {
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
    private val PARAM_TITLE = TransitionParam(name = "sectionTitle", type = String::class)
    private val PARAM_TEXT = TransitionParam(name = "sectionText", type = String::class)

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
    private val PARAM_IMAGE_PROMPT = TransitionParam(
        name = "DALL-E image prompt",
        type = String::class,
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
    override val input: List<TransitionParam<*>> = emptyList()

    override fun transition(state: ArticleState, input: InputMap): Either<String, ArticleState> {
        return Either.Right(
            ArticleState.Finished(state.data)
        )
    }
}