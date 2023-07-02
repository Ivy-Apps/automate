package automate.domain.article

import automate.domain.article.data.Article
import automate.domain.article.data.BodyItem

fun String.wordsCount(): Int = split(" ").size

fun Article.sections(): List<String> = body.map {
    when (it) {
        is BodyItem.Section -> it.title
    }
}