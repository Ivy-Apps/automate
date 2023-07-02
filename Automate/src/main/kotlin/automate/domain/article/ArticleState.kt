package automate.domain.article

import automate.Constants
import automate.domain.article.data.Article
import automate.statemachine.State

sealed interface ArticleState : State<Article> {
    object Initial : ArticleState {
        override val data = Article(
            topic = Constants.ARTICLE_TOPIC,
            title = "",
            body = emptyList()
        )
    }

    data class Writing(
        override val data: Article
    ) : ArticleState

    data class AddedImage(
        override val data: Article
    ) : ArticleState

    data class Finished(
        override val data: Article
    ) : ArticleState {
        override val isFinal = true
    }
}