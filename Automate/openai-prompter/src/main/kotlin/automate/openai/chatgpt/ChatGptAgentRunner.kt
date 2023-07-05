package automate.openai.chatgpt

import automate.statemachine.impl.StateMachine

class ChatGptAgentRunner(
    private val stateMachine: StateMachine,
    private val prompter: ChatGptPrompter
) {
    suspend fun run() {
        stateMachine.run { transitions ->
            TODO()
        }
    }
}