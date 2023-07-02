package automate.domain.article

import automate.domain.article.data.Article
import automate.domain.article.data.BodyItem

fun String.wordsCount(): Int = split(" ").size

fun Article.sections(): List<String> = body.mapNotNull {
    when (it) {
        is BodyItem.Image -> null
        is BodyItem.Section -> it.title
    }
}