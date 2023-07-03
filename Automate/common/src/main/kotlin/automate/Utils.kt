package automate

fun String.normalizePrompt(): String = trimIndent()
    .trim().replace("\t", "")