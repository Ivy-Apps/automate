package automate.article.statemachine

import automate.article.ArticleConstants
import automate.article.data.Article
import automate.statemachine.State

sealed interface ArticleState : State<Article> {
    object SetTitle : ArticleState {
        override val data = Article(
            topic = ArticleConstants.ARTICLE_TOPIC,
            title = "",
            introduction = "",
            body = emptyList(),
            conclusion = "",
        )
        override val expectedOutcome = "setTitle"
    }

    data class WriteIntroduction(
        override val data: Article,
    ) : ArticleState {
        override val expectedOutcome = "writeSection"
    }

    data class WriteBody(
        override val data: Article
    ) : ArticleState {
        override val expectedOutcome = "writeBody"
    }

    data class Conclusion(
        override val data: Article
    ) : ArticleState {
        override val isFinal = true
        override val expectedOutcome = "writeConclusion"
    }
}