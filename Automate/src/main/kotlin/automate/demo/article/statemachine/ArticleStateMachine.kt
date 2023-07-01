package automate.demo.article.statemachine

import automate.demo.article.Article
import automate.demo.article.ai.ArticleChatGptPrompter
import automate.statemachine.InputMap
import automate.statemachine.StateMachine
import javax.inject.Inject

class ArticleStateMachine @Inject constructor(
    private val articleChatGptPrompter: ArticleChatGptPrompter,
) : StateMachine<ArticleState, ArticleTransition, Article>(
    initialState = ArticleState.Initial,
    maxErrors = 1,
    maxSteps = 15,
) {
    override fun availableTransitions(state: ArticleState): List<ArticleTransition> {
        return when (state) {
            ArticleState.Initial -> listOf(
                SetTitleTransition,
            )

            is ArticleState.AddedImage -> listOf(AddSectionTransition)
            is ArticleState.Writing -> listOf(
                AddSectionTransition,
                AddImageTransition,
                FinalizeTransition
            )

            is ArticleState.Finished -> listOf()
        }
    }

    override suspend fun nextTransition(
        state: ArticleState,
        availableTransitions: List<ArticleTransition>,
        error: String?
    ): Pair<ArticleTransition, InputMap> {
        return articleChatGptPrompter.prompt(
            state = state,
            error = error,
            maxSteps = maxSteps,
            availableTransition = availableTransitions,
        )
    }

}