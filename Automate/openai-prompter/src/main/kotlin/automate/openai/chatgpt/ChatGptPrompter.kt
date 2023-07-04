package automate.openai.chatgpt

import arrow.core.Either
import arrow.core.raise.catch
import automate.openai.chatgpt.data.ChatGptResponse
import automate.openai.chatgpt.data.Choice
import automate.openai.chatgpt.data.InputParameter
import automate.statemachine.InputMap
import automate.statemachine.Transition
import automate.statemachine.data.Feedback
import automate.statemachine.prompt.StateMachinePrompter
import kotlinx.serialization.json.Json

abstract class ChatGptPrompter<A : Any, S : State<A>, Trans : Transition<S, A>>(
    private val chatGptService: ChatGptApi,
    private val chatGptPrePrompter: ChatGptPrePrompter,
) : StateMachinePrompter<A, S, Trans> {

    /**
     * a writer, an Android Developer
     */
    protected abstract fun aLabel(): String

    protected abstract fun taskPrompt(): String

    protected abstract fun example(): ChatGptPrePrompter.Example

    protected abstract fun currentStateAsJson(
        state: S,
        data: A,
        options: List<Choice>,
        feedback: List<Feedback>,
        choicesLeft: Int,
    ): String

    protected abstract fun refineData(state: S): A

    override suspend fun prompt(
        state: S,
        feedback: List<Feedback>,
        availableTransitions: List<Trans>,
        steps: Int,
        maxSteps: Int,
    ): Either<Feedback.Error, Pair<Trans, InputMap>> = catch({
        val prompt = currentStateAsJson(
            state = state,
            data = refineData(state),
            options = availableTransitions.toOptions(),
            feedback = feedback,
            choicesLeft = maxSteps - steps,
        )

        val prePrompt = chatGptPrePrompter.prePromptList(
            modelLabel = aLabel(),
            taskPrompt = taskPrompt(),
            example = example()
        )
        val responseJson = chatGptService.prompt(
            conversation = prePrompt + ChatGptMessage(
                role = ChatGptRole.User,
                content = prompt,
            )
        )
        val response = parseChatGptResponse(responseJson)

        val choiceIndex = response.choiceId - 1
        val transition = try {
            availableTransitions[choiceIndex]
        } catch (e: IndexOutOfBoundsException) {
            availableTransitions[choiceIndex - 1]
        }
        Either.Right(
            transition to (response.input ?: emptyMap())
        )
    }) {
        it.printStackTrace()
        Either.Left(
            Feedback.Error(
                feedback = "$it"
            )
        )
    }

    private fun parseChatGptResponse(responseJson: String): ChatGptResponse {
        return try {
            Json.decodeFromString<ChatGptResponse>(responseJson)
        } catch (e: Exception) {
            try {
                // Add missing '}'
                Json.decodeFromString<ChatGptResponse>("$responseJson}")
            } catch (e: Exception) {
                // Remove duplicated '}'
                Json.decodeFromString<ChatGptResponse>(responseJson.dropLast(1))
            }
        }
    }

    protected fun List<Trans>.toOptions(): List<Choice> {
        return mapIndexed { index, transition ->
            Choice(
                choiceId = index + 1, // start from 1 because ChatGPT is a retard
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
}
