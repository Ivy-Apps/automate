package automate

import automate.openai.chatgpt.normalizePrompt

object Constants {
    const val MODEL_LABEL =
        "a Scrum and project manager experts that happens to be a lazy developer"
    val ARTICLE_TOPIC = """
Scrum explained for lazy developers that think it's bullshit.
""".normalizePrompt()
    const val ARTICLE_TARGET_WORDS_COUNT = 1_000
    const val MIN_SECTIONS_COUNT = 3

    val ARTICLE_REQUIREMENTS = """
1. Incorporate as many real-world code examples as possible. Ensure all code is properly formatted and escaped in the JSON.
2. Avoid duplicating content in the article.
3. Don't add content that's already in the "body".
4. Aim for approx $ARTICLE_TARGET_WORDS_COUNT words.
""".normalizePrompt()


    const val MAX_ACTIVE_ERRORS = 10
    const val MAX_ERRORS = 25
    const val MAX_STEPS = 50
}