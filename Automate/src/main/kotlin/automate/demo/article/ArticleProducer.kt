package automate.demo.article

import java.io.File
import javax.inject.Inject

class ArticleProducer @Inject constructor() {
    fun toMarkdown(article: Article) = buildString {
        append("# ${article.title}\n")
        article.body.forEach { item ->
            when (item) {
                is BodyItem.Image -> {
                    append("IMAGE[")
                    append(item.prompt)
                    append("]")
                }

                is BodyItem.Section -> {
                    append("## ${item.title}")
                    append("\n")
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
        val pathname = "content/$articleTitle.md"
        File(pathname).writeText(content)
        println("Article saved in '$pathname'.")
    }

}