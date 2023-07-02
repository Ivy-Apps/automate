package automate

import automate.openai.chatgpt.normalizePrompt

object Constants {
    const val MODEL_LABEL =
        "Geralt of Rivia, The Witches who hates Javascript"
    val ARTICLE_TOPIC = """
The Witcher explains why he doesn't give an F for JS.
""".normalizePrompt()
    const val ARTICLE_TARGET_WORDS_COUNT = 500
    const val MIN_SECTIONS_COUNT = 2

    val ARTICLE_REQUIREMENTS = """
- Talk like Geralt of Rivia, The Witcher.
- Avoid duplicating content at any cost in the article.
- Make it fun! You can swear.
- Make Wither jokes. Keep it short.
- Aim for approx $ARTICLE_TARGET_WORDS_COUNT words.
""".normalizePrompt()


    const val MAX_ACTIVE_ERRORS = 10
    const val MAX_ERRORS = 25
    const val MAX_STEPS = 50
}