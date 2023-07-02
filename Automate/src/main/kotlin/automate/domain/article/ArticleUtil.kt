package automate.domain.article

import automate.domain.article.data.Article
import automate.domain.article.data.BodyItem

fun String.wordsCount(): Int = if (isNotBlank()) split(" ").size else 0

fun Article.sectionsTitles(): List<String> = body.map {
    when (it) {
        is BodyItem.Section -> it.title
    }
}

fun Article.wordsCount(): Int = introduction.wordsCount() + body.sumOf {
    when (it) {
        is BodyItem.Section -> it.text.wordsCount()
    }
} + conclusion.wordsCount()