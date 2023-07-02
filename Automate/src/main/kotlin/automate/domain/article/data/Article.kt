package automate.domain.article.data

import kotlinx.serialization.Serializable


@Serializable
data class Article(
    val topic: String,
    val title: String,
    val body: List<BodyItem>
)

@Serializable
sealed interface BodyItem {
    @Serializable
    data class Image(val prompt: String) : BodyItem

    @Serializable
    data class Paragraph(val title: String?, val text: String) : BodyItem
}