package automate.openai.chatgpt

import arrow.core.nonEmptyListOf
import automate.openai.chatgpt.data.ChatGptAgent
import automate.openai.log
import automate.openai.transition
import automate.statemachine.data.StateMachineError
import automate.statemachine.impl.InputDef
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank
import io.mockk.mockk

class ChatGptPrompterTest : FreeSpec({
    "pre-prompting" {
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
        val translator = ChatGptPrompter(agent, mockk(), ChatGptResponseParser())

        // when
        val prePrompt = translator.prePrompt()

        // then
        log("Pre-prompt: ${prePrompt.length} chars")
        log(prePrompt)
        prePrompt.shouldNotBeBlank()
    }

    "prompting current state" {
        // given
        val agent = ChatGptAgent(
            goal = "goal",
            requirements = nonEmptyListOf("a", "b", "c"),
            behavior = "behavior"
        )
        val translator = ChatGptPrompter(agent, mockk(), ChatGptResponseParser())

        // when
        val statePrompt = translator.statePrompt(
            state = mapOf(
                "a" to "A",
                "b" to "B"
            ),
            transitions = nonEmptyListOf(
                transition(
                    name = "trans_a",
                    description = "Transition a",
                    inputs = listOf(
                        InputDef(
                            name = "a",
                            description = "Desc a"
                        ),
                        InputDef(
                            name = "b",
                            description = "Desc b"
                        ),
                    )
                ),
                transition(
                    name = "b",
                    inputs = listOf(
                        InputDef(
                            name = "var",
                            description = "Desc var",
                        )
                    )
                ),
            ),
            lastError = StateMachineError.TransitionProvider("Error 1,2,3")
        )

        // then
        log("State prompt: ${statePrompt.length} chars")
        log(statePrompt)
        statePrompt shouldBe """
            <<GOAL>>
            goal
            <</GOAL>>
            <<REQUIREMENTS>>
            1. a
            2. b
            3. c
            <</REQUIREMENTS>>
            <<BEHAVIOR>>
            behavior
            <</BEHAVIOR>>
            <<STATE>>
            a: A
            b: B
            <</STATE>>
            <<CHOICES>>
            <<OPTION 1>>
            "trans_a": Transition a
            <<INPUT A>>
            Desc a
            <</INPUT A>>
            <<INPUT B>>
            Desc b
            <</INPUT B>>
            <</OPTION 1>>
            <<OPTION 2>>
            "b"
            <<INPUT VAR>>
            Desc var
            <</INPUT VAR>>
            <</OPTION 2>>
            <</CHOICES>>
            <<ERROR>>
            Error 1,2,3
            <</ERROR>>
        """.trimIndent()
    }
})