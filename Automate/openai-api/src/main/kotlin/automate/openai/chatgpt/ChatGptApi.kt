package automate.openai.chatgpt

import automate.KtorClient
import automate.di.AppScope
import automate.di.SingleIn
import automate.openai.OpenAiSecrets
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import javax.inject.Inject

@SingleIn(AppScope::class)
class ChatGptApi @Inject constructor(
    private val openAiSecrets: OpenAiSecrets,
    private val ktorClient: KtorClient,
    private val chatGptConversionLogger: ChatGptConversionLogger,
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
                    ChatGptApiRequest(
                        model = MODEL_GPT_3_5_TURBO_16k,
                        messages = conversation,
                    )
                )
            }
        }
        require(response.status.isSuccess()) {
            "Response unsuccessful! $response"
        }

        val replyContent = response.body<ChatGptApiResponse>().choices.first().message.content
        chatGptConversionLogger.log(
            conversation = conversation,
            response = replyContent,
        )
        return replyContent
    }
}