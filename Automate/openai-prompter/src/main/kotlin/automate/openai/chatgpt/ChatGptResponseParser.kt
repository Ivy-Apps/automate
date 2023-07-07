package automate.openai.chatgpt

import automate.statemachine.impl.InputsMap
import automate.statemachine.impl.NextTransitionProviderScope
import automate.statemachine.impl.Transition
import javax.inject.Inject

class ChatGptResponseParser @Inject constructor() {

    suspend fun NextTransitionProviderScope.parse(
        transitions: List<Transition>,
        chatGptResponse: String
    ): Pair<Transition, InputsMap> {

        TODO()
    }
}