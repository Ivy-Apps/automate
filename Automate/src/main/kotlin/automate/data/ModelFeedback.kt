package automate.data

import kotlinx.serialization.Serializable

@Serializable
sealed interface ModelFeedback {
    val feedbackType: String
    val description: String
    val feedback: String

    @Serializable
    data class FatalError(
        override val feedback: String,
    ) : ModelFeedback {
        override val feedbackType = "FATAL_ERROR"
        override val description =
            "A critical error that immediately halts the program. This error requires immediate resolution."
    }

    @Serializable
    data class Error(
        override val feedback: String,
    ) : ModelFeedback {
        override val feedbackType = "ERROR"
        override val description = """
            High-priority error that should not be repeated. The "resolved" boolean indicates whether this error has been addressed.
        """.trimIndent()
    }

    @Serializable
    data class Suggestion(
        override val feedback: String
    ) : ModelFeedback {
        override val feedbackType = "SUGGESTION"
        override val description =
            "A recommendation for improving future prompts. These suggestions are lower priority, but should be considered for enhancing the model's responses."
    }
}