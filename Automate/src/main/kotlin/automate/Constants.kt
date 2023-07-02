package automate

object Constants {
    val ARTICLE_TOPIC = """
    Explain Svelte with many practical code examples.
    """.trimIndent()
    val ARTICLE_REQUIREMENTS = """
    Requirements:
    1. Use many code examples. The code must be properly escaped in the JSON.
    2. It must NOT have repeated article sections.    
    3. Each paragraph must be short.
    4. Target length: around 1024 words.
    """.trimIndent()

    const val MAX_STEPS = 30
    const val MAX_ERRORS = 6
    const val MAX_EXCEPTION_LENGTH = 120
    const val MAX_BODY_SECTIONS_HISTORY = 7
}