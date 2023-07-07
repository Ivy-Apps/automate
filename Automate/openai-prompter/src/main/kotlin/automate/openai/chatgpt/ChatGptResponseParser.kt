package automate.openai.chatgpt

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import automate.openai.chatgpt.ChatGptPrompter.Companion.OPEN_TAG_END
import automate.openai.chatgpt.ChatGptPrompter.Companion.OPEN_TAG_START
import automate.openai.chatgpt.ChatGptPrompter.Companion.closeTag
import automate.parser.ParserDslScope
import automate.parser.parse
import automate.statemachine.impl.InputsMap
import automate.statemachine.impl.Transition
import javax.inject.Inject

class ChatGptResponseParser @Inject constructor() {

    /**
     * <<OPTION 1>>
     * <<INPUT A>>
     * To start, let's discuss the concept of variables, we must.
     * <</INPUT A>>
     * <<INPUT B>>
     * Variables, they are containers for data, they store.
     * <</INPUT B>>
     * <</OPTION 1>>
     */
    fun parse(
        transitions: NonEmptyList<Transition>,
        chatGptResponse: String
    ): Either<String, Pair<Transition, InputsMap>> = either {
        parse(chatGptResponse) {
            consumeWhitespace()
            val optionIndex = parseOptionIndex().bind()
            val transition = transitions.getOrNull(optionIndex)
            ensureNotNull(transition) {
                "Invalid option index: $optionIndex. Choose an option between [1, ${transitions.size}]."
            }

            val inputs = buildList {
                while (true) {
                    consumeWhitespace()
                    when (val maybeInput = parseInput()) {
                        is Either.Left -> break
                        is Either.Right -> add(maybeInput.value)
                    }
                }
            }.toMap()

            transition to inputs
        }
    }

    private fun ParserDslScope.parseOptionIndex(): Either<String, Int> = either {
        ensureNotNull(consumeString("${OPEN_TAG_START}OPTION")) {
            "Couldn't parse the selected option. It must start with ${ChatGptPrompter.openTag("OPTION X")} tag."
        }
        consumeWhitespace()
        val optionIndexText = consumeUntil(endTag = OPEN_TAG_END, dropEndTag = true)
        ensureNotNull(optionIndexText) {
            "Couldn't parse the selected option. It must start with ${ChatGptPrompter.openTag("OPTION X")} tag."
        }
        val index = optionIndexText.toIntOrNull()
        ensureNotNull(index) {
            "Invalid option. '$index' is not a valid option integer."
        }
        return@either index - 1 // because our array starts from zero
    }

    private fun ParserDslScope.parseInput(): Either<Unit, Pair<String, String>> =
        either {
            ensureNotNull(consumeString("${OPEN_TAG_START}INPUT")) {}
            consumeWhitespace()
            val inputName = consumeUntil(endTag = OPEN_TAG_END, dropEndTag = true)
            ensureNotNull(inputName) {}
            consumeWhitespace()
            val inputValue = consumeUntil(
                endTag = closeTag("INPUT $inputName"),
                dropEndTag = true
            )?.trim()
            ensureNotNull(inputValue) {}
            inputName to inputValue
        }


}