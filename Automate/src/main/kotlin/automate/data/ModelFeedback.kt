package automate.data

import kotlinx.serialization.Serializable

@Serializable
sealed interface ModelFeedback {
    val type: String
    val description: String
    val feedback: String

    @Serializable
    data class FatalError(
        override val feedback: String,
    ) : ModelFeedback {
        override val type = "FATAL_ERROR"
        override val description = "An error that immediately terminates the program execution."
    }

    @Serializable
    data class Error(
        override val feedback: String,
        val resolved: Boolean = false,
    ) : ModelFeedback {
        override val type = "ERROR"
        override val description = """
            Errors are of highest importance that must be fixed immediately!
            The "resolved" boolean will indicate whether they are fixed.
        """.trimIndent()
    }

    @Serializable
    data class Suggestion(
        override val feedback: String
    ) : ModelFeedback {
        override val type = "SUGGESTION"
        override val description = "Suggestions are of lower priority and should be considered only for next prompts."
    }
}