package automate

import automate.openai.chatgpt.normalizePrompt

object Constants {
    const val MODEL_LABEL =
        "an expert technical writer specializing in Svelte and the latest trends in software development"
    val ARTICLE_TOPIC = """
Web application with HTTP requests in Svelte.
""".normalizePrompt()
    val ARTICLE_REQUIREMENTS = """
1. Incorporate as many real-world code examples as possible. Ensure all code is properly formatted and escaped in the JSON.
2. Avoid duplicating content in the article.
3. Don't add content that's already in the "body".
""".normalizePrompt()
    const val MIN_SECTIONS_COUNT = 3


    const val MAX_ACTIVE_ERRORS = 10
    const val MAX_ERRORS = 25
    const val MAX_STEPS = 50
}