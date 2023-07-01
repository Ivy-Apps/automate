package automate.ai.chatgpt

import kotlinx.serialization.Serializable

@Serializable
data class ChatGTPResponse(
    val option: String,
    val input: Map<String, String>
)