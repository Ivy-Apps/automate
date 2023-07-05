package automate.openai.chatgpt

import arrow.core.nonEmptyListOf
import automate.openai.log
import automate.openai.transition
import automate.statemachine.data.StateMachineError
import automate.statemachine.impl.InputDef
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank
import io.mockk.mockk

class ChatGptTranslatorTest : FreeSpec({
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
        val translator = ChatGptPrompter(agent, mockk())

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
        val translator = ChatGptPrompter(agent, mockk())

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
            [[GOAL_START]]
            goal
            [[GOAL_END]]
            [[REQUIREMENTS_START]]
            - a
            - b
            - c
            [[REQUIREMENTS_END]]
            [[BEHAVIOR_START]]
            behavior
            [[BEHAVIOR_END]]
            [[STATE_START]]
            a: A
            b: B
            [[STATE_END]]
            [[CHOICES_START]]
            [[OPTION 1_START]]
            trans_a
            Transition a
            [[INPUT A_START]]
            Desc a
            [[INPUT A_END]]
            [[INPUT B_START]]
            Desc b
            [[INPUT B_END]]
            [[OPTION 1_END]]
            [[OPTION 2_START]]
            b
            [[INPUT VAR_START]]
            Desc var
            [[INPUT VAR_END]]
            [[OPTION 2_END]]
            [[CHOICES_END]]
            [[ERROR_START]]
            Error 1,2,3
            [[ERROR_END]]
        """.trimIndent()
    }
})