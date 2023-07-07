package automate.openai.chatgpt

import automate.di.AppScope
import automate.di.SingleIn
import java.io.File
import javax.inject.Inject

@SingleIn(AppScope::class)
class ChatGptConversionLogger @Inject constructor(
) {
    private var prePromptLogged = false

    private val logFile by lazy {
        File("chat.log").apply {
            writeText("")
        }
    }

    fun log(
        conversation: List<ChatGptMessage>,
        response: String,
    ) {
        conversation.filter {
            !prePromptLogged || it.role != ChatGptRole.System
        }.onEach { message ->
            logMessage(
                author = "[${message.role}] ${message.name ?: "user"}",
                content = message.content
            )
        }
        prePromptLogged = true

        logMessage(
            author = "[Chat GPT] Response",
            content = response,
        )
    }

    private fun logMessage(
        author: String,
        content: String
    ) {
        val text = buildString {
            append("$author:\n")
            append(content)
            append("\n\n")
        }
        logFile.appendText(text)
    }
}