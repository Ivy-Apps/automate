package automate.openai.chatgpt

import arrow.core.Either
import arrow.core.raise.catch
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

    /**
     * a writer, an Android Developer
     */
    protected abstract fun aLabel(): String

    protected abstract fun taskPrompt(): String

    protected abstract fun example(): Pair<String, ChatGptReply>

    protected abstract fun currentStateAsJson(
        state: S,
        data: A,
        options: List<Choice>,
        feedback: List<ModelFeedback>,
        choicesLeft: Int,
    ): String

    protected abstract fun refineData(state: S): A

    private fun modelContext(): List<ChatGptMessage> = buildList {
        add(
            ChatGptMessage(
                role = ChatGptRole.System,
                content = """
You are ${aLabel()}, a pattern-following assistant using JSON for communication. 
You'll receive:
- "data": The current status of the task at hand.
- "choices": A set of options each having its own specific input.
- "feedback": Instructions to rectify errors and make better future decisions.
- "choicesLeft": The count of remaining choices before completing the task.

Make sure to:
- Adhere to valid JSON formatting rules and ensure proper escaping.
- Pick an option from "choices" and provide the necessary "input" for it.
""".normalizePrompt()
            )
        )
        add(
            ChatGptMessage(
                role = ChatGptRole.System,
                content = """
Your task is to compile an article:
'''
${taskPrompt().normalizePrompt()}
'''
Continue by selecting appropriate options until the task is completed.
""".normalizePrompt()
            )
        )

        val (exampleStateJson, exampleResponse) = example()

        add(
            ChatGptMessage(
                role = ChatGptRole.System,
                name = "example_user",
                content = exampleStateJson,
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
        availableTransitions: List<Trans>,
        steps: Int,
        maxSteps: Int,
    ): Either<ModelFeedback.Error, Pair<Trans, InputMap>> = catch({
        val prompt = currentStateAsJson(
            state = state,
            data = refineData(state),
            options = availableTransitions.toOptions(),
            feedback = feedback,
            choicesLeft = maxSteps - steps,
        )

        val responseJson = chatGptService.prompt(
            conversation = modelContext() + ChatGptMessage(
                role = ChatGptRole.User,
                content = prompt,
            )
        )
        val response = parseChatGptResponse(responseJson)

        val choiceIndex = choiceIndexWithFallback(response, availableTransitions)
        Either.Right(
            availableTransitions[choiceIndex] to (response.input ?: emptyMap())
        )
    }) {
        it.printStackTrace()
        Either.Left(
            ModelFeedback.Error(
                feedback = "Exception: $it."
            )
        )
    }

    private fun parseChatGptResponse(responseJson: String): ChatGptReply {
        return try {
            Json.decodeFromString<ChatGptReply>(responseJson)
        } catch (e: Exception) {
            try {
                // Add missing '}'
                Json.decodeFromString<ChatGptReply>("$responseJson}")
            } catch (e: Exception) {
                // Remove duplicated '}'
                Json.decodeFromString<ChatGptReply>(responseJson.dropLast(1))
            }
        }
    }

    private fun choiceIndexWithFallback(
        response: ChatGptReply,
        availableTransition: List<Trans>
    ): Int {
        return try {
            response.choice.first().code - 'A'.code
        } catch (e: Exception) {
            if (availableTransition.size == 1) {
                0
            } else {
                throw e
            }
        }
    }

    protected fun List<Trans>.toOptions(): List<Choice> {
        return mapIndexed { index, transition ->
            Choice(
                choice = ('A' + index).toString(),
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
        val choice: String,
        val input: Map<String, String>? = null
    )

    interface CurrentState<A> {
        val data: A
        val expectedOutcome: String
        val choices: List<Choice>
        val feedback: List<String>
        val choicesLeft: Int
    }

    @Serializable
    data class Choice(
        val choice: String,
        val title: String,
        val description: String?,
        val input: List<InputParameter>? = null
    )

    @Serializable
    data class InputParameter(
        val name: String,
        val description: String? = null,
    )
}

fun String.normalizePrompt(): String = trimIndent()
    .trim().replace("\t", "")

