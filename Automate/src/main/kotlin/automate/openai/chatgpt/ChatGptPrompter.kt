package automate.openai.chatgpt

import automate.statemachine.InputMap
import automate.statemachine.State
import automate.statemachine.Transition
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

abstract class ChatGptPrompter<A : Any, S : State<A>, Trans : Transition<S, A>>(
    private val chatGptService: ChatGptService,
) {
    protected abstract fun goal(data: A): String

    protected abstract fun currentStateJson(state: S, error: String?): String

    suspend fun prompt(
        state: S,
        error: String?,
        maxSteps: Int,
        availableTransition: List<Trans>
    ): Pair<Trans, InputMap> {

        val prompt = buildString {
            val currentStateJson = currentStateJson(state, error)
            append(
                """
                Current state:
                $currentStateJson
                
                Choose an option:
                ${availableTransition.toOptionsMenu()}
            """.trimIndent()
            )
        }

        val responseJson = chatGptService.prompt(
            conversation = listOf(
                ChatGptMessage(
                    role = ChatGptRole.System,
                    content = prePrompt(state.data, maxSteps)
                ),
                ChatGptMessage(
                    role = ChatGptRole.User,
                    content = prompt,
                )
            )
        )

        val response = Json.decodeFromString<ChatGTPResponse>(responseJson)
        val optionIndex = response.option.first().code - 'A'.code
        return availableTransition[optionIndex] to (response.input ?: emptyMap())
    }

    private fun List<Trans>.toOptionsMenu(): String {
        var letter = 'A'

        return buildString {
            this@toOptionsMenu.forEach { transition ->
                append("${letter++}. ")
                append(transition.name)
                transition.input.forEach { param ->
                    append("<")
                    append("${param.name}: ${param.type.simpleName}")
                    if (param.description != null) {
                        append("; ${param.description}")
                    }
                    append("> ")
                }
                append('\n')
            }
        }
    }

    private fun prePrompt(data: A, maxSteps: Int): String = """
            I'll give you a goal that you must accomplish in maximum $maxSteps steps.
            You must respond only with valid JSON.
            On each step I'll give you the available choice you can make.
            
            For example:
            A. Set article title <title: String>
            B. Add section <sectionTitle: String> <sectionText: String; supports Markdown>
            C. Finalize article
            
            An example of a valid response if you choose Option B would be:
            {
                "option": "B",
                "input" : {
                    "sectionTitle": "Some section",
                    "sectionText": "Some **text**\n- point 1\n- point 2\nThis is random."
                }
            }
            
            Then:
            I'll respond to you with:
            - A JSON representing the current state of your work.
            - A new list of available options and their input parameters.
            
            You must again respond only with a JSON of the option that you think
            will move the state closer to achieving the goal.
            Your response must be in this JSON format:
            {
                "option": "C" // the letter of the option
                "input" : {
                    // zero or many input parameters
                }
            }
            
            ---
            
            Here is your goal.
            GOAL: "${goal(data)}".
            You must complete the goal defined above by only responding with a JSON
            with one of the options that I provide you.            
        """.trimIndent()

    @Serializable
    data class ChatGTPResponse(
        val option: String,
        val input: Map<String, String>? = null
    )
}

