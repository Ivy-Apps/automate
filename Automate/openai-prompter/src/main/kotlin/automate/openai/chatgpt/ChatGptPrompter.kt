package automate.openai.chatgpt

import arrow.core.Either
import arrow.core.NonEmptyList
import automate.openai.chatgpt.data.ChatGptAgentError
import automate.openai.chatgpt.data.ChatGptParams
import automate.openai.normalizePrompt
import automate.statemachine.data.StateMachineError
import automate.statemachine.impl.InputsMap
import automate.statemachine.impl.NextTransitionProviderScope
import automate.statemachine.impl.Transition
import org.jetbrains.annotations.VisibleForTesting

class ChatGptPrompter(
    private val params: ChatGptParams,
    private val api: ChatGptApi,
    private val responseParser: ChatGptResponseParser
) {
    companion object {
        const val OPEN_TAG_START = "<<"
        const val OPEN_TAG_END = ">>"

        const val CLOSE_TAG_START = "<</"
        const val CLOSE_TAG_END = ">>"

        fun openTag(name: String) = "$OPEN_TAG_START${name.uppercase()}$OPEN_TAG_END"
        fun closeTag(name: String) = "$CLOSE_TAG_START${name.uppercase()}$CLOSE_TAG_END"
    }

    @VisibleForTesting
    internal fun prePrompt(): String = """
You are a pattern-following assistant that lives in a state-machine to achieve the following goal:
"${params.goal}"

The task requirements are:
${params.requirements.toNumbersList()}

You're role is '${params.behavior}' and must role-play this behavior.

You must only respond in the following format that resembles XML:
- Sections start with ${openTag("section")} and end with ${closeTag("section")} tags.
- On each message you'll receive:
1. ${openTag("state")}: The state of the task
2. ${openTag("choices")} with ${openTag("option 1")}, ${openTag("option 2")}, ${openTag("option N")} inside them.
3. An option inside ${openTag("choices")} looks like this and has zero or many inputs.
${openTag("Option 1")}
A description of the option and what it does.
${openTag("Input A")}
A variable named "a" that does...
${closeTag("Input A")}
${openTag("Input B")}
Lorem ipsum
${closeTag("Input B")}
${closeTag("Option 1")}
4. After analyzing the ${openTag("state")} and choosing the best option you
must respond with:
${openTag("Option X")}
${openTag("Input A")}
This is a start of the value of the variable "a"
${closeTag("Input A")}
${openTag("Input B")}
Value for "b"
${closeTag("Input B")}
${closeTag("Option X")}
5. If you make an invalid or a bad choice you'll receive an additional ${openTag("error")}
with information what went wrong. You must adjust your next choice based on the information
in the error.
""".normalizePrompt()

    private val _messages: MutableList<Message> = mutableListOf()
    private val messages = _messages

    suspend fun prompt(
        scope: NextTransitionProviderScope,
        transitions: NonEmptyList<Transition>,
    ): Pair<Transition, InputsMap> = with(scope) {
        val prompt = statePrompt(data, transitions, lastError)
        val conversation = buildList {
            add(
                ChatGptMessage(
                    role = ChatGptRole.System,
                    content = prePrompt(),
                )
            )

            messages.lastOrNull()?.let {
                add(ChatGptMessage(role = ChatGptRole.User, content = it.prompt))
                add(ChatGptMessage(role = ChatGptRole.Assistant, content = it.chatGptResponse))
            }

            add(
                ChatGptMessage(
                    role = ChatGptRole.User,
                    content = prompt
                )
            )
        }

        val chatGptResponse = when (val res = api.prompt(conversation)) {
            is Either.Left -> {
                when (val chatGptError = res.value) {
                    is ChatGptApi.Error.Generic -> error(chatGptError.error)
                    ChatGptApi.Error.ServiceUnavailable -> {
                        throw ChatGptAgentError("ChatGPT service unavailable. Multiple 5xx errors.")
                    }
                }
            }

            is Either.Right -> res.value
        }

        when (val res = responseParser.parse(transitions, chatGptResponse)) {
            is Either.Left -> error("ParseError: $res")
            is Either.Right -> res.value
        }
    }

    @VisibleForTesting
    internal fun statePrompt(
        state: Map<String, Any>,
        transitions: NonEmptyList<Transition>,
        lastError: StateMachineError?,
    ): String = buildString {
        start("goal")
        append(params.goal)
        end("goal")
        append('\n')
        start("requirements")
        append(params.requirements.toNumbersList())
        end("requirements")
        append('\n')
        start("behavior")
        append(params.behavior)
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

    private fun List<String>.toNumbersList(): String {
        val list = this
        return buildString {
            list.forEachIndexed { index, requirement ->
                append("${index + 1}. $requirement")
                if (index + 1 < list.size) {
                    append('\n')
                }
            }
        }
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
        append('"')
        append(transition.name)
        append('"')
        if (transition.description != null) {
            append(": ")
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
        append(openTag(tag))
        append('\n')
    }

    private fun StringBuilder.end(tag: String) {
        append('\n')
        append(closeTag(tag))
    }

    data class Message(
        val prompt: String,
        val chatGptResponse: String,
    )
}