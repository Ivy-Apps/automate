package automate.openai.chatgpt.data

import kotlinx.serialization.Serializable


@Serializable
data class ChatGptReply(
    val choiceId: Int,
    val input: Map<String, String>? = null
)

@Serializable
data class Choice(
    val choiceId: Int,
    val title: String,
    val description: String?,
    val input: List<InputParameter>? = null
)

@Serializable
data class InputParameter(
    val name: String,
    val description: String? = null,
)