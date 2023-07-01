package automate.demo.article

import javax.inject.Inject

class ArticlePrinter @Inject constructor() {
    fun asString(article: Article) = buildString {
        append("# ${article.title}")
        article.body.forEach { item ->
            when (item) {
                is BodyItem.Image -> {
                    append("IMAGE[")
                    append(item.prompt)
                    append("]")
                }

                is BodyItem.Section -> {
                    append("## ${item.title}")
                    append(item.text)
                    append("\n\n")
                }
            }
        }
    }
}