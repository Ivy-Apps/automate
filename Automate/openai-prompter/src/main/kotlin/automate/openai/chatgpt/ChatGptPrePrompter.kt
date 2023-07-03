package automate.openai.chatgpt

import automate.openai.chatgpt.data.ChatGptResponse
import automate.openai.normalizePrompt
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ChatGptPrePrompter @Inject constructor(
) {

    fun prePromptList(
        modelLabel: String,
        taskPrompt: String,
        example: Example,
    ): List<ChatGptMessage> = buildList {
        add(
            ChatGptMessage(
                role = ChatGptRole.System,
                content = """
You are ${modelLabel.normalizePrompt()}, a pattern-following assistant using JSON for communication. 
You'll receive:
- "article": The current state of the article.
- "choices": A set of options each having its own specific input.
- "feedback": Instructions to rectify errors and make better future decisions.

Make sure to:
- Adhere to valid JSON formatting rules and ensure proper escaping.
- Pick an option from "choices" and provide the necessary "input" for it.
""".normalizePrompt()
            )
        )
        add(
            ChatGptMessage(
                role = ChatGptRole.System,
                content = """
Your task is to compile an article:
'''
${taskPrompt.normalizePrompt()}
'''
Continue by selecting appropriate options until the task is completed.
""".normalizePrompt()
            )
        )

        add(
            ChatGptMessage(
                role = ChatGptRole.System,
                name = "example_user",
                content = example.userPrompt,
            )
        )
        add(
            ChatGptMessage(
                role = ChatGptRole.System,
                name = "example_assistant",
                content = Json.encodeToString(example.chatGptResponse)
            )
        )
    }

    data class Example(
        val userPrompt: String,
        val chatGptResponse: ChatGptResponse,
    )
}