package automate.openai.chatgpt

import automate.statemachine.impl.StateMachine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ChatGptAgent(
    private val stateMachine: StateMachine,
    private val prompter: ChatGptPrompter,
) {
    private val _data = MutableStateFlow(emptyMap<String, Any>())
    val dataFlow: StateFlow<Map<String, Any>> = _data

    suspend fun run(): StateMachine.Completion {
        return stateMachine.run { transitions ->
            _data.value = data
            prompter.prompt(this, transitions)
        }
    }
}