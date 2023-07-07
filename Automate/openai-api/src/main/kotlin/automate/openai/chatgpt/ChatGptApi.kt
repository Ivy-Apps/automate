package automate.openai.chatgpt

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.catch
import arrow.core.right
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
        const val MAX_RETRIES = 3
    }

    suspend fun prompt(
        conversation: List<ChatGptMessage>,
        attempt: Int = 1,
    ): Either<Error, String> = catch({
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
        if (response.status.value in 500..599) {
            return@catch if (attempt <= MAX_RETRIES) {
                prompt(conversation, attempt = attempt + 1)
            } else {
                Error.ServiceUnavailable.left()
            }
        } else if (!response.status.isSuccess()) {
            return@catch Error.Generic("Response: ${response.status}").left()
        }

        val replyContent = response.body<ChatGptApiResponse>()
            .choices.first().message.content
        chatGptConversionLogger.log(
            conversation = conversation,
            response = replyContent,
        )

        replyContent.right()
    }) {
        Error.Generic(it.message ?: "Unknown").left()
    }

    sealed interface Error {
        object ServiceUnavailable : Error
        data class Generic(val error: String) : Error {
            override fun toString(): String {
                return "ChatGPT API error: $error."
            }
        }
    }
}