package automate.ai.chatgpt

import automate.statemachine.State
import automate.statemachine.Transition

abstract class ChatGptPrompter<A, S : State<A>, Trans : Transition<S, A>> {
    protected abstract fun goal(data: A): String

    protected abstract fun stateToJson(state: S): String

    private var prePrompted = false

    suspend fun prompt(
        state: S,
        maxSteps: Int,
        availableTransition: List<Trans>
    ): Trans {
        val prompt = buildString {
            if (!prePrompted) {
                append(prePrompt(state.data, maxSteps))
                prePrompted = true
            }
            append(
                """
                Current state:
                ${stateToJson(state)}
                
                Choose an option:
                ${availableTransition.toOptionsMenu()}
            """.trimIndent()
            )
        }
        println("PROMPT:")
        println(prompt)
        println("END PROMPT ---------------")

        println("Enter the ChatGPT response JSON:")
        val responseJson = readln()
        TODO("Not yet implemented")
    }

    private fun List<Trans>.toOptionsMenu(): String {
        var letter = 'A'

        return buildString {
            this@toOptionsMenu.forEach { transition ->
                append("${letter++}. ")
                append(transition.name)
                transition.input.forEach { param ->
                    append("<")
                    append("${param.name}: ${param.type}")
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
            C. Finalize article [FINAL]
            
            An example of a valid response if you choose Option B would be:
            {
                "option": "B",
                "sectionTitle": "Some section",
                "sectionText": "Some **text**\n- point 1\n- point 2\nThis is random."
            }
            
            Then:
            I'll respond to you with:
            - A JSON representing the current state of your work.
            - A new list of available options and their input parameters.
            
            You must again respond with a JSON of the option that you think
            will move the state closer to achieving the goal.
            
            ---
            
            Here is your goal.
            GOAL: "${goal(data)}".
            You must complete the goal defined above by only responding with a JSON
            with one of the options that I provide you.
            
            ---
            
            
        """
}