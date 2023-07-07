package automate.openai.chatgpt

import automate.openai.chatgpt.data.ChatGptParams
import automate.openai.chatgpt.di.ChatPrompterFactory
import automate.statemachine.impl.StateMachine
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ChatGptAgent @AssistedInject constructor(
    @Assisted
    private val stateMachine: StateMachine,
    @Assisted
    private val params: ChatGptParams,
    private val chatPrompterFactory: ChatPrompterFactory,
) {
    private val _data = MutableStateFlow(emptyMap<String, Any>())
    val dataFlow: StateFlow<Map<String, Any>> = _data

    suspend fun run(): StateMachine.Completion {
        val prompter = chatPrompterFactory.create(params)
        return stateMachine.run { transitions ->
            _data.value = data
            prompter.prompt(this, transitions)
        }
    }
}