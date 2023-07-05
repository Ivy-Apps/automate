package automate.openai.chatgpt

import arrow.core.NonEmptyList

data class ChatGptAgent(
    val goal: String,
    val requirements: NonEmptyList<String>,
    val behavior: String,
)