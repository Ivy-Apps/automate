package automate.openai.chatgpt

import arrow.core.Either
import arrow.core.NonEmptyList
import automate.openai.normalizePrompt
import automate.statemachine.data.StateMachineError
import automate.statemachine.impl.Transition
import org.jetbrains.annotations.VisibleForTesting
import javax.inject.Inject

class ChatGptPrompter @Inject constructor(
    private val agent: ChatGptAgent,
    private val api: ChatGptApi,
) {
    companion object {
        fun startTag(name: String) = "`[${name.uppercase()}_START]`"
        fun endTag(name: String) = "`[${name.uppercase()}_END]`"
    }

    @VisibleForTesting
    fun prePrompt(): String = """
You are a pattern-following assistant that lives in a state-machine
to achieve the following goal: "${agent.goal}" by completing the task.
The task requirements are:
${agent.requirements.toBulletList()}

You're role is '${agent.behavior}' and must role-play this behavior.

You must only respond with in this format:
- Sections start with ${startTag("sectionName")} and ends with ${endTag("sectionName")}.
- On each message you'll receive:
1. ${startTag("state")}: The state of the task
2. ${startTag("choices")} with ${startTag("option 1")}, 
${startTag("option 2")}, ${startTag("option N")} inside them.
3. Each ${startTag("option X")} has zero or many "Inputs".
- You must respond by inspecting ${startTag("state")} and responding with
the best ${startTag("option X")} providing the proper string inputs
to complete the task and achieve the goal.
4. An option inside ${startTag("choices")} looks like this:
${startTag("Option 1")}
A description of the option and what it does.
${startTag("Input A")}
A variable named "a" that does...
${endTag("Input A")}
${startTag("Input B")}
Lorem ipsum
${endTag("Input B")}
${endTag("Option 1")}
5. After analyzing the ${startTag("state")} and choosing the best option you
must respond with:
${startTag("Option X")}
${startTag("Input A")}
This is a start of the value of the variable "a"
${endTag("Input A")}
${startTag("Input B")}
Value for "b"
${endTag("Input B")}
${endTag("Option X")}
6. If you make an invalid or a bad choice you'll receive an additional ${startTag("error")}
with information what went wrong. You must adjust your next choice based on the information
in the ${startTag("error")}.
""".normalizePrompt()

    suspend fun prompt(
        state: Map<String, Any>,
        transitions: NonEmptyList<Transition>,
        firstPrompt: Boolean,
    ): Either<ChatGptApi.Error, String> {

        TODO()
    }

    @VisibleForTesting
    fun statePrompt(
        state: Map<String, Any>,
        transitions: NonEmptyList<Transition>,
        lastError: StateMachineError?,
    ): String = buildString {
        start("goal")
        append(agent.goal)
        end("goal")
        append('\n')
        start("requirements")
        append(agent.requirements.toBulletList())
        end("requirements")
        append('\n')
        start("behavior")
        append(agent.behavior)
        end("behavior")
        append('\n')
        start("state")
        state(state)
        end("state")
        append('\n')
        start("choices")
        options(transitions)
        end("choices")
        if (lastError != null) {
            append('\n')
            start("error")
            append(lastError.error)
            end("error")
        }
    }

    private fun List<String>.toBulletList(): String {
        return "- ${joinToString("\n- ")}"
    }

    private fun StringBuilder.state(state: Map<String, Any>) {
        var index = 0
        state.forEach { (key, value) ->
            if (index++ > 0) {
                append('\n')
            }
            append(key)
            append(": ")
            append(value)
        }
    }

    private fun StringBuilder.options(transitions: NonEmptyList<Transition>) {
        transitions.forEachIndexed { index, transition ->
            if (index > 0) {
                append('\n')
            }
            option(index, transition)
        }
    }

    private fun StringBuilder.option(index: Int, transition: Transition) {
        start("Option ${index + 1}")
        append(transition.name)
        if (transition.description != null) {
            append("\n")
            append(transition.description)
        }
        for (input in transition.inputs) {
            append('\n')
            start("Input ${input.name}")
            append(input.description)
            end("Input ${input.name}")
        }
        end("Option ${index + 1}")
    }

    private fun StringBuilder.start(tag: String) {
        append(startTag(tag))
        append('\n')
    }

    private fun StringBuilder.end(tag: String) {
        append('\n')
        append(endTag(tag))
    }
}