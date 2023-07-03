package automate.statemachine.data

import kotlinx.serialization.Serializable

@Serializable
sealed interface ModelFeedback {
    val feedback: String

    @Serializable
    data class FatalError(
        override val feedback: String,
    ) : ModelFeedback

    @Serializable
    data class Error(
        override val feedback: String,
    ) : ModelFeedback

    @Serializable
    data class Suggestion(
        override val feedback: String
    ) : ModelFeedback
}