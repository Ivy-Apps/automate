package automate.domain.article

import automate.Constants
import automate.domain.article.data.Article
import automate.statemachine.StateMachine
import javax.inject.Inject

class ArticleStateMachine @Inject constructor(
    articleChatGptPrompter: ArticleChatGptPrompter,
) : StateMachine<ArticleState, ArticleTransition, Article>(
    initialState = ArticleState.SetTitle,
    maxActiveErrors = Constants.MAX_ACTIVE_ERRORS,
    maxErrors = Constants.MAX_ERRORS,
    maxSteps = Constants.MAX_STEPS,
) {
    override val prompter = articleChatGptPrompter

    override fun availableTransitions(state: ArticleState): List<ArticleTransition> {
        val body = state.data.body
        return when (state) {
            ArticleState.SetTitle -> {
                listOf(SetTitleTransition)
            }

            is ArticleState.WriteIntroduction -> {
                listOf(WriteIntroduction)
            }

            is ArticleState.WriteBody -> {
                buildList {
                    add(AddSectionTransition)
                    if (body.size > 5) {
                        add(WriteConclusionTransition)
                    }
                }
            }

            is ArticleState.Conclusion -> {
                emptyList()
            }
        }
    }
}