package automate.openai.chatgpt

import arrow.core.Either
import arrow.core.raise.catch
import automate.openai.chatgpt.data.ChatGptReply
import automate.openai.chatgpt.data.Choice
import automate.openai.chatgpt.data.InputParameter
import automate.openai.normalizePrompt
import automate.statemachine.InputMap
import automate.statemachine.State
import automate.statemachine.Transition
import automate.statemachine.data.ModelFeedback
import automate.statemachine.prompt.StateMachinePrompter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

abstract class ChatGptPrompter<A : Any, S : State<A>, Trans : Transition<S, A>>(
    private val chatGptService: ChatGptApi,
) : StateMachinePrompter<A, S, Trans> {

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
- "article": The current state of the article.
- "choices": A set of options each having its own specific input.
- "feedback": Instructions to rectify errors and make better future decisions.

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

    override suspend fun prompt(
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
            ModelFeedback.Error(
                feedback = "$it"
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
