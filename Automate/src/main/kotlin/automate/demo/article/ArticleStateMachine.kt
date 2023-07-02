package automate.demo.article

import automate.Constants
import automate.demo.article.data.Article
import automate.statemachine.StateMachine
import javax.inject.Inject

class ArticleStateMachine @Inject constructor(
    articleChatGptPrompter: ArticleChatGptPrompter,
) : StateMachine<ArticleState, ArticleTransition, Article>(
    initialState = ArticleState.Initial,
    maxErrors = Constants.MAX_ERRORS,
    maxSteps = Constants.MAX_STEPS,
) {
    override val prompter = articleChatGptPrompter

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

}