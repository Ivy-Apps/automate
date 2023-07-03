package automate.article.statemachine

import automate.article.ArticleConstants
import automate.article.data.Article
import automate.article.prompt.ArticleChatGptPrompter
import automate.article.sectionsTitles
import automate.article.wordsCount
import automate.statemachine.StateMachine
import javax.inject.Inject

class ArticleStateMachine @Inject constructor(
    articleChatGptPrompter: ArticleChatGptPrompter,
) : StateMachine<ArticleState, ArticleTransition, Article>(
    initialState = ArticleState.SetTitle,
    maxActiveErrors = ArticleConstants.MAX_ACTIVE_ERRORS,
    maxErrors = ArticleConstants.MAX_ERRORS,
    maxSteps = ArticleConstants.MAX_STEPS,
) {
    override val prompter = articleChatGptPrompter

    override fun availableTransitions(state: ArticleState): List<ArticleTransition> {
        val article = state.data
        val body = article.body
        return when (state) {
            ArticleState.SetTitle -> {
                listOf(SetTitleTransition)
            }

            is ArticleState.WriteIntroduction -> {
                listOf(WriteIntroduction)
            }

            is ArticleState.WriteBody -> {
                if (article.wordsCount() > ArticleConstants.ARTICLE_TARGET_WORDS_COUNT + 200) {
                    listOf(WriteConclusionTransition)
                } else {
                    buildList {
                        add(AddSectionTransition)
                        if (article.sectionsTitles().size >= ArticleConstants.MIN_SECTIONS_COUNT) {
                            add(WriteConclusionTransition)
                        }
                    }
                }
            }

            is ArticleState.Conclusion -> {
                emptyList()
            }
        }
    }
}