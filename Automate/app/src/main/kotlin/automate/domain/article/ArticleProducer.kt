package automate.domain.article

import automate.domain.article.data.Article
import automate.domain.article.data.BodyItem
import java.io.File
import java.time.LocalDateTime
import javax.inject.Inject

class ArticleProducer @Inject constructor() {
    fun toMarkdown(article: Article) = buildString {
        append("# ${article.title}\n")
        append("\n${article.introduction}")
        append("\n\n")
        article.body.forEach { item ->
            when (item) {
                is BodyItem.Section -> {
                    append("### ${item.title}")
                    append("\n")
                    append(item.text)
                    append("\n\n")
                }
            }
        }
        append("\n\n\n")
        append("### Conclusion")
        append("\n\n${article.conclusion}")
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
        val pathname = "content/[${LocalDateTime.now()}] ${articleTitle}.md"
        File(pathname).writeText(content)
        println("Article saved in '$pathname'.")
    }

}