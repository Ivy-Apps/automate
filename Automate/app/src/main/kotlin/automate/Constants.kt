package automate

import automate.openai.chatgpt.normalizePrompt

object Constants {
    const val MODEL_LABEL =
        "expert build engineer specializing in Kotlin and Gradle Kotlin DSL"
    val ARTICLE_TOPIC = """
Gradle Kotlin DSL modularization
""".normalizePrompt()
    const val ARTICLE_TARGET_WORDS_COUNT = 1024
    const val MIN_SECTIONS_COUNT = 2

    val ARTICLE_REQUIREMENTS = """     
- Make it practical         
- Avoid duplicating content at any cost in the article.
- Make it fun! 
- Aim for approx $ARTICLE_TARGET_WORDS_COUNT words.
""".normalizePrompt()


    const val MAX_ACTIVE_ERRORS = 10
    const val MAX_ERRORS = 25
    const val MAX_STEPS = 50
}