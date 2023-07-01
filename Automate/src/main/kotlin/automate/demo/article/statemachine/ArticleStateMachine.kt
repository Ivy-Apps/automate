package automate.demo.article.statemachine

import automate.demo.article.Article
import automate.statemachine.InputMap
import automate.statemachine.StateMachine


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