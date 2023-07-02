package automate.demo.article

import automate.demo.article.data.Article
import automate.demo.article.data.BodyItem
import java.io.File
import java.time.LocalDateTime
import javax.inject.Inject

class ArticleProducer @Inject constructor() {
    fun toMarkdown(article: Article) = buildString {
        append("# ${article.title}\n")
        article.body.forEach { item ->
            when (item) {
                is BodyItem.Image -> {
                    append("\n\n**IMAGE[")
                    append(item.prompt)
                    append("]**\n\n")
                }

                is BodyItem.Paragraph -> {
                    if (item.title != null) {
                        append("### ${item.title}")
                        append("\n")
                    }
                    append(item.text)
                    append("\n\n")
                }
            }
        }
    }

    fun saveInFile(article: Article) {
        val fileContent = toMarkdown(article)
        saveFile(
            articleTitle = article.title,
            content = fileContent,
        )
    }

    private fun saveFile(
        articleTitle: String,
        content: String
    ) {
        val pathname = "content/$articleTitle (${LocalDateTime.now()}).md"
        File(pathname).writeText(content)
        println("Article saved in '$pathname'.")
    }

}