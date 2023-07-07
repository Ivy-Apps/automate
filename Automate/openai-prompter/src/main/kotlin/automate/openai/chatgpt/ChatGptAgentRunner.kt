package automate.openai.chatgpt

import automate.statemachine.impl.StateMachine
import javax.inject.Inject

class ChatGptAgentRunner @Inject constructor(
    private val stateMachine: StateMachine,
    private val prompter: ChatGptPrompter
) {
    suspend fun run() {
        stateMachine.run { transitions ->
            prompter.prompt(this, transitions)
        }
    }
}