package automate.openai.chatgpt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class ChatGptRole {
    @SerialName("system")
    System,

    @SerialName("user")
    User,

    @SerialName("assistant")
    Assistant,

    @SerialName("function")
    Function;
}

@Serializable
data class ChatGptApiRequest(
    val model: String,
    val messages: List<ChatGptMessage>,
)

@Serializable
data class ChatGptApiResponse(
    val choices: List<ChatGptChoice>,
)

@Serializable
data class ChatGptChoice(
    val index: Int,
    val message: ChatGptMessage,
)

@Serializable
data class ChatGptMessage(
    val role: ChatGptRole,
    val content: String,
    val name: String? = null,
)