package automate

import automate.openai.chatgpt.normalizePrompt

object Constants {
    val ARTICLE_TOPIC = """
    Svelte web development explained in practice with short and simple code examples.
    """.trimIndent()
    val ARTICLE_REQUIREMENTS = """
    1. Use real-life code examples. The code must be properly escaped in the JSON.
    2. It must NOT have repeated article sections.    
    3. Each paragraph must be short.
    4. Target length: around 1024 words.
    """.normalizePrompt()

    const val MAX_ACTIVE_ERRORS = 2
    const val MAX_ERRORS = 12
    const val MAX_STEPS = 30
    const val MAX_EXCEPTION_LENGTH = 120
    const val MAX_BODY_SECTIONS_HISTORY = 5
}