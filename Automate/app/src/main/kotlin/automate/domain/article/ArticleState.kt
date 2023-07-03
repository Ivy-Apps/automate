package automate.domain.article

import automate.Constants
import automate.domain.article.data.Article
import automate.statemachine.State

sealed interface ArticleState : State<Article> {
    object SetTitle : ArticleState {
        override val data = Article(
            topic = Constants.ARTICLE_TOPIC,
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