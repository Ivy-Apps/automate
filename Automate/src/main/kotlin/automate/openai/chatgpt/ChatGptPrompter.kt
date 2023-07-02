package automate.openai.chatgpt

import arrow.core.Either
import arrow.core.raise.catch
import automate.Constants
import automate.data.ModelFeedback
import automate.openai.chatgpt.network.ChatGptMessage
import automate.openai.chatgpt.network.ChatGptRole
import automate.openai.chatgpt.network.ChatGptService
import automate.statemachine.InputMap
import automate.statemachine.State
import automate.statemachine.Transition
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

abstract class ChatGptPrompter<A : Any, S : State<A>, Trans : Transition<S, A>>(
    private val chatGptService: ChatGptService,
) {
    companion object {
        const val FINALIZE_TAG = "[FINALIZE]"
    }

    /**
     * a writer, an Android Developer
     */
    protected abstract fun aLabel(): String

    protected abstract fun taskPrompt(): String

    protected abstract fun example(): Pair<CurrentState<A>, ChatGptReply>


    private fun modelContext(
    ): List<ChatGptMessage> = buildList {
        add(
            ChatGptMessage(
                role = ChatGptRole.System,
                content = """
                You're ${aLabel()} and a pattern-following assistant that communicates using JSON.
                You receive:
                - "currentState": the state of the task that you're completing.
                - "choices": list of available choices with their input.
                - "feedback": feedback to guide your next choice.
                - "choicesLeft": the # of choices that you can make before finalizing the task.
                
                You must respond with:
                - valid a JSON following the pattern
                - You must choose a choice from the "choices" list providing all "input" for the given choice.
                """.normalizePrompt()
            )
        )
        add(
            ChatGptMessage(
                role = ChatGptRole.System,
                content = """
                Your task is to:
                '''
                ${taskPrompt()}
                '''
                by making the optimum number of choices until the task is achieved.
                When ready you must finalize the goal with the "$FINALIZE_TAG" choices before
                going out of choices, tracked by "choicesLeft".
                """.normalizePrompt()
            )
        )

        val (exampleState, exampleResponse) = example()

        add(
            ChatGptMessage(
                role = ChatGptRole.System,
                name = "example_user",
                content = Json.encodeToString(exampleState)
            )
        )
        add(
            ChatGptMessage(
                role = ChatGptRole.System,
                name = "example_assistant",
                content = Json.encodeToString(exampleResponse)
            )
        )
    }

    suspend fun prompt(
        state: S,
        feedback: List<ModelFeedback>,
        availableTransition: List<Trans>,
        steps: Int,
        maxSteps: Int,
    ): Either<ModelFeedback.Error, Pair<Trans, InputMap>> = catch({
        val currentState = CurrentState(
            currentState = state,
            options = availableTransition.toOptions(),
            feedback = feedback,
            choicesLeft = maxSteps - steps,
        )
        val prompt = Json.encodeToString(currentState)

        val responseJson = chatGptService.prompt(
            conversation = modelContext() + ChatGptMessage(
                role = ChatGptRole.User,
                content = prompt,
            )
        )

        val response = Json.decodeFromString<ChatGptReply>(responseJson)
        val optionIndex = response.option.first().code - 'A'.code
        Either.Right(
            availableTransition[optionIndex] to (response.input ?: emptyMap())
        )
    }) {
        it.printStackTrace()
        Either.Left(
            ModelFeedback.Error(
                feedback = "Exception: ${
                    it.message?.take(Constants.MAX_EXCEPTION_LENGTH) ?: it
                }."
            )
        )
    }

    protected fun List<Trans>.toOptions(): List<Option> {
        return mapIndexed { index, transition ->
            Option(
                key = ('A' + index).toString(),
                title = transition.name,
                description = transition.description,
                input = transition.input.map { param ->
                    InputParameter(
                        name = param.name,
                        description = param.description,
                    )
                }
            )
        }
    }

    @Serializable
    data class ChatGptReply(
        val option: String,
        val input: Map<String, String>? = null
    )

    @Serializable
    data class CurrentState<A>(
        val currentState: A,
        val options: List<Option>,
        val feedback: List<ModelFeedback>,
        val choicesLeft: Int,
    )

    @Serializable
    data class Option(
        val key: String,
        val title: String,
        val description: String?,
        val input: List<InputParameter>? = null
    )

    @Serializable
    data class InputParameter(
        val name: String,
        val description: String? = null,
    )

    private fun String.normalizePrompt(): String = trimIndent().trim()
}

