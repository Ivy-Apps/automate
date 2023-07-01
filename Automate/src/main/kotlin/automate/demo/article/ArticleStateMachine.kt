package automate.demo.article

import arrow.core.Either
import automate.statemachine.*


class ArticleStateMachine : StateMachine<ArticleState, ArticleTransition, Article>(
    initialState = ArticleState.Initial
) {
    override fun availableTransitions(state: ArticleState): List<ArticleTransition> {
        return when (state) {
            ArticleState.Initial -> listOf(
                AddSectionTransition,
                AddImageTransition,
            )

            is ArticleState.AddedImage -> listOf(AddSectionTransition)
            is ArticleState.Writing -> listOf(
                AddSectionTransition,
                AddImageTransition
            )

            is ArticleState.Finished -> listOf()
        }
    }

    override suspend fun nextTransition(
        availableTransitions: List<ArticleTransition>,
        error: String?
    ): Pair<ArticleTransition, InputMap> {
        TODO("Not yet implemented")
    }

}