package automate.statemachine.data

import kotlinx.serialization.Serializable

@Serializable
sealed interface StateMachineError {
    val error: String

    @Serializable
    data class TransitionProviderError(
        override val error: String,
    ) : StateMachineError

    @Serializable
    data class TransitionError(
        override val error: String,
    ) : StateMachineError
}