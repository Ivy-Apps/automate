package automate

import automate.openai.chatgpt.normalizePrompt

object Constants {
    const val MODEL_LABEL =
        "an expert technical writer specializing in Kotlin and the latest trends in software development"
    val ARTICLE_TOPIC = """
Svelte for beginners build your first reactive website with real-world code examples.
""".normalizePrompt()
    val ARTICLE_REQUIREMENTS = """
1. Incorporate real-world code examples. Ensure all code is properly formatted and escaped in the JSON.
2. Avoid duplicating content in the article.
3. Keep paragraphs concise for readability.
4. The total length of the article should be approximately 1024 words.
""".normalizePrompt()

    const val MAX_ACTIVE_ERRORS = 2
    const val MAX_ERRORS = 12
    const val MAX_STEPS = 30
}