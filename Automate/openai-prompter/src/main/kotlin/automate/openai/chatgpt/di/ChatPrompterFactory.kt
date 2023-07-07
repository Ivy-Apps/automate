package automate.openai.chatgpt.di

import automate.openai.chatgpt.ChatGptPrompter
import automate.openai.chatgpt.data.ChatGptParams
import dagger.assisted.AssistedFactory

@AssistedFactory
interface ChatPrompterFactory {
    fun create(params: ChatGptParams): ChatGptPrompter
}