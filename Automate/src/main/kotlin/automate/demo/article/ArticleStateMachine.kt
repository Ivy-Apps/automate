package automate.demo.article

import automate.Constants
import automate.demo.article.data.Article
import automate.demo.article.data.BodyItem
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
        val body = state.data.body
        return when (state) {
            ArticleState.Initial -> {
                listOf(SetTitleTransition)
            }

            is ArticleState.AddedImage -> {
                listOf(AddParagraphTransition)
            }

            is ArticleState.Writing -> {
                buildList {
                    add(AddImageTransition)
                    if (!body.lastNAreParagraphs(4)) {
                        add(AddParagraphTransition)
                    }
                    if (body.size > 3) {
                        add(FinalizeTransition)
                    }
                }
            }

            is ArticleState.Finished -> listOf()
        }
    }

    private fun List<BodyItem>.lastNAreParagraphs(n: Int): Boolean {
        var count = 0
        var index = size - 1
        while (count < n) {
            if (getOrNull(index) !is BodyItem.Paragraph) return false
            count++
            index--
        }
        return true
    }

}