package automate.openai.chatgpt

import arrow.core.nonEmptyListOf
import automate.openai.transition
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FreeSpec

class ChatGptResponseParserTest : FreeSpec({
    "parses an easy response" {
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
            transitions = nonEmptyListOf(
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

    "parse harder response" {
        // given
        val parser = ChatGptResponseParser()
        val response = """
            <<OPTION 1>>
            "Hook": Write an engaging first words on the topic.
            <<INPUT HOOK>>
            "Instruct you, I will. All core concepts, teach I must!".
            <</INPUT HOOK>>
            <</OPTION 1>>
        """.trimIndent()

        // when
        val option1 = transition(name = "Option 1")
        val res = parser.parse(
            transitions = nonEmptyListOf(
                option1,
                transition(name = "Option 2")
            ),
            chatGptResponse = response
        )

        // then
        res.shouldBeRight(
            option1 to mapOf(
                "HOOK" to "\"Instruct you, I will. All core concepts, teach I must!\"."
            )
        )
    }
})