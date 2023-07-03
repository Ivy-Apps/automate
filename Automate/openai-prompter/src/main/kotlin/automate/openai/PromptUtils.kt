package automate.openai

fun String.normalizePrompt(): String = trimIndent()
    .trim().replace("\t", "")