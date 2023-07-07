package automate.openai.chatgpt.di

import automate.openai.chatgpt.ChatGptAgent
import automate.openai.chatgpt.data.ChatGptParams
import automate.statemachine.impl.StateMachine
import dagger.assisted.AssistedFactory

@AssistedFactory
interface ChatGptAgentFactory {
    fun create(
        params: ChatGptParams,
        stateMachine: StateMachine,
    ): ChatGptAgent
}