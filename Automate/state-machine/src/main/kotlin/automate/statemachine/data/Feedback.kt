package automate.statemachine.data

import kotlinx.serialization.Serializable

@Serializable
sealed interface Feedback {
    val feedback: String

    @Serializable
    data class FatalError(
        override val feedback: String,
    ) : Feedback

    @Serializable
    data class Error(
        override val feedback: String,
    ) : Feedback

    @Serializable
    data class Warning(
        override val feedback: String
    ) : Feedback
}