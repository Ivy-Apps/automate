package automate

import automate.openai.chatgpt.normalizePrompt

object Constants {
    const val MODEL_LABEL =
        "Grandmaster Yoda from Star Wars who learn to code and hates Java"
    val ARTICLE_TOPIC = """
Explain like Yoda why Java is not good and it should be deleted. Keep it short.
""".normalizePrompt()
    const val ARTICLE_TARGET_WORDS_COUNT = 700
    const val MIN_SECTIONS_COUNT = 3

    val ARTICLE_REQUIREMENTS = """
- Talk like Yoda.
- Avoid duplicating content in the article.
- Aim for approx $ARTICLE_TARGET_WORDS_COUNT words.
- Make it fun!
""".normalizePrompt()


    const val MAX_ACTIVE_ERRORS = 10
    const val MAX_ERRORS = 25
    const val MAX_STEPS = 50
}