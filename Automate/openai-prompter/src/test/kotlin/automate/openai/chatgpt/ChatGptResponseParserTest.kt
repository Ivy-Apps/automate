package automate.openai.chatgpt

import automate.openai.transition
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FreeSpec

class ChatGptResponseParserTest : FreeSpec({
    "parses a response" {
        // given
        val parser = ChatGptResponseParser()
        val response = """
            <<OPTION 1>>
            <<INPUT A>>
            To start, let's discuss the concept of variables, we must.
            <</INPUT A>>
            <<INPUT B>>
            Variables, they are containers for data, they store.
            <</INPUT B>>
            <</OPTION 1>>
        """.trimIndent()

        // when
        val option1 = transition(name = "Option 1")
        val res = parser.parse(
            transitions = listOf(
                option1,
                transition(name = "Option 2")
            ),
            chatGptResponse = response
        )

        // then
        res.shouldBeRight(
            option1 to mapOf(
                "A" to "To start, let's discuss the concept of variables, we must.",
                "B" to "Variables, they are containers for data, they store."
            )
        )
    }
})