package automate.demo.article


data class Article(
    val title: String,
    val body: List<BodyItem>
)

sealed interface BodyItem {
    data class Image(val prompt: String) : BodyItem
    data class Section(val title: String, val text: String) : BodyItem
}