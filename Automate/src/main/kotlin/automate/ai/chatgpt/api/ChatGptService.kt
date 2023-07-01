package automate.ai.chatgpt.api

import automate.KtorClient
import automate.ai.OpenAiSecrets
import automate.di.AppScope
import automate.di.SingleIn
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import javax.inject.Inject

@SingleIn(AppScope::class)
class ChatGptService @Inject constructor(
    private val openAiSecrets: OpenAiSecrets,
    private val ktorClient: KtorClient,
) {
    companion object {
        const val API_URL = "https://api.openai.com/v1/chat/completions"
        const val MODEL_GPT_3_5_TURBO_16k = "gpt-3.5-turbo-16k"
        const val MODEL_GTP_3_5_TURBO = "gpt-3.5-turbo"
    }

    suspend fun prompt(
        conversation: List<ChatGptMessage>
    ): String {
        val response = ktorClient.execute {
            post(API_URL) {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer ${openAiSecrets.apiKey}")
                setBody(
                    ChatGptRequest(
                        model = MODEL_GPT_3_5_TURBO_16k,
                        messages = conversation,
                    )
                )
            }
        }
        require(response.status.isSuccess()) {
            "Response unsuccessful! $response"
        }

        return response.body<ChatGptResponse>().choices.first().message.content
    }
}

enum class ChatGptRole(val stringValue: String) {
    System("system"),
    User("user"),
    Assistant("assistant");
}

@Serializable
data class ChatGptRequest(
    val model: String,
    val messages: List<ChatGptMessage>,
)

@Serializable
data class ChatGptResponse(
    val choices: List<ChatGptChoice>,
)

@Serializable
data class ChatGptChoice(
    val index: Int,
    val message: ChatGptMessage,
)

@Serializable
data class ChatGptMessage(
    val role: String,
    val content: String,
)