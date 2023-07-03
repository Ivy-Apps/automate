package automate.article.prompt

import automate.Constants
import automate.article.data.Article
import automate.article.data.BodyItem
import automate.article.sectionsTitles
import automate.article.wordsCount
import kotlinx.serialization.Serializable

@Serializable
data class ArticleGptOptimized(
    val title: String,
    val topic: String,
    val currentWords: Int,
    val targetWords: Int,
    val introduction: String,
    val sectionsTitles: List<String>,
    val body: List<BodyItem>,
)

internal fun Article.optimizeForChatGpt(): ArticleGptOptimized {
    return ArticleGptOptimized(
        title = title,
        topic = topic,
        currentWords = wordsCount(),
        targetWords = Constants.ARTICLE_TARGET_WORDS_COUNT,
        introduction = introduction,
        sectionsTitles = sectionsTitles(),
        body = body,
    )
}