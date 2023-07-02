package automate.domain.article

import automate.Constants
import automate.domain.article.data.Article
import automate.domain.article.data.BodyItem
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

            is ArticleState.AddedImage -> {
                listOf(AddSectionTransition)
            }

            is ArticleState.WriteBody -> {
                buildList {
                    add(AddImageTransition)
                    if (!body.lastNAreParagraphs(4)) {
                        add(AddSectionTransition)
                    }
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

    private fun List<BodyItem>.lastNAreParagraphs(n: Int): Boolean {
        var count = 0
        var index = size - 1
        while (count < n) {
            if (getOrNull(index) !is BodyItem.Section) return false
            count++
            index--
        }
        return true
    }

}