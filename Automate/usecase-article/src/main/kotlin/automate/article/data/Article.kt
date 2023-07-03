package automate.article.data

import kotlinx.serialization.Serializable

@Serializable
data class Article(
    val topic: String,
    val title: String,
    val introduction: String,
    val body: List<BodyItem>,
    val conclusion: String,
)

@Serializable
sealed interface BodyItem {
    @Serializable
    data class Section(val title: String, val text: String) : BodyItem
}