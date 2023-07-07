package automate.openai.chatgpt.di

import automate.openai.chatgpt.ChatGptAgent
import automate.openai.chatgpt.ChatGptApi
import automate.openai.chatgpt.ChatGptPrompter
import automate.openai.chatgpt.ChatGptResponseParser
import automate.openai.chatgpt.data.ChatGptParams
import automate.statemachine.impl.StateMachine
import javax.inject.Inject

class ChatGptAgentFactory @Inject constructor(
    private val api: ChatGptApi,
    private val responseParser: ChatGptResponseParser,
) {
    fun create(
        params: ChatGptParams,
        stateMachine: StateMachine,
    ): ChatGptAgent {
        return ChatGptAgent(
            stateMachine = stateMachine,
            prompter = ChatGptPrompter(
                params = params,
                api = api,
                responseParser = responseParser,
            )
        )
    }
}