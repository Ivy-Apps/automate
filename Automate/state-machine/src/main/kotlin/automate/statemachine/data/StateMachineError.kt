package automate.statemachine.data

import kotlinx.serialization.Serializable

@Serializable
sealed interface StateMachineError {
    val error: String

    @Serializable
    data class TransitionProvider(
        override val error: String,
    ) : StateMachineError

    @Serializable
    data class Transition(
        override val error: String,
    ) : StateMachineError
}