package automate.openai.chatgpt

import arrow.core.nonEmptyListOf
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.string.shouldNotBeBlank

class ChatGptTranslatorTest : FreeSpec({
    "pre-prompt the model" {
        // given
        val agent = ChatGptAgent(
            goal = "Write a short LinkedIn post teaching all core concepts in programming.",
            requirements = nonEmptyListOf(
                "Talk like Yoda.",
                "Teach like Yoda.",
                "Teach the core concepts like variables, types, branching, functions.",
                "Teach all the concepts that makes programming possible."
            ),
            behavior = "Grandmaster Yoda from Star Wars who also invented computer programming"
        )
        val translator = ChatGptPrompter(agent)

        // when
        val prePrompt = translator.prePrompt()

        // then
        println(prePrompt)
        prePrompt.shouldNotBeBlank()
    }
})