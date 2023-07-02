package automate.demo.article.statemachine

import automate.demo.article.Article
import automate.statemachine.State

sealed interface ArticleState : State<Article> {
    object Initial : ArticleState {
        override val data = Article(
            topic = "Using the ChatGPT API with Kotlin and Ktor",
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