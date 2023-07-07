package automate.openai.chatgpt.data

import arrow.core.NonEmptyList

data class ChatGptParams(
    val goal: String,
    val requirements: NonEmptyList<String>,
    val behavior: String,
)