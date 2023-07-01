package automate.demo.article

import automate.statemachine.State

sealed interface ArticleState : State<Article> {
    object Initial : ArticleState {
        override val data = Article(
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