package automate.statemachine

import automate.statemachine.data.StateMachineError
import automate.statemachine.impl.StateMachine.Completion
import automate.statemachine.impl.Transition
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe

class StateMachineTest : FreeSpec({
    "terminates" - {
        "because of transition provider errors" {
            // given
            val stateMachine = stateMachine(maxErrors = 3) {
                initialState("s") {
                    transition("a") {
                        goTo("s")
                    }
                }
            }

            // when
            val completion = stateMachine.run {
                error("err")
            }

            // then
            completion shouldBe Completion.MaxErrorsReached(
                listOf(
                    StateMachineError.TransitionProvider("err"),
                    StateMachineError.TransitionProvider("err"),
                    StateMachineError.TransitionProvider("err"),
                )
            )
            stateMachine.steps.shouldBeEmpty()
            stateMachine.data shouldBe emptyMap()
        }

        "because of transition errors" {
            // given
            val stateMachine = stateMachine(maxErrors = 3) {
                initialState("s") {
                    transition("a") {
                        goTo("b")
                    }
                }
            }

            // when
            val completion = stateMachine.run { transitions ->
                transitions.first() to emptyMap()
            }

            // then
            completion shouldBe Completion.MaxErrorsReached(
                listOf(
                    StateMachineError.Transition("Invalid next state 'b'."),
                    StateMachineError.Transition("Invalid next state 'b'."),
                    StateMachineError.Transition("Invalid next state 'b'."),
                )
            )
            stateMachine.steps.shouldBeEmpty()
            stateMachine.data shouldBe emptyMap()
        }

        "because of max steps reached" {
            // given
            val stateMachine = stateMachine(maxSteps = 3) {
                initialState("a") {
                    transition("a") {
                        goTo("a")
                    }
                }
            }

            // when
            var transition: Transition? = null
            val completion = stateMachine.run { transitions ->
                transition = transitions.first()
                transition!! to emptyMap()
            }
            val executableTransition = transition!!.prepare(emptyMap())

            // then
            val steps = listOf(
                executableTransition,
                executableTransition,
                executableTransition,
            )
            completion shouldBe Completion.MaxStepsReached(
                steps = steps
            )
            stateMachine.steps shouldBe steps
            stateMachine.data shouldBe emptyMap()
        }

        "because a final state is reached" {
            // given
            val stateMachine = stateMachine {
                initialState("a") {
                    transition("go to b") {
                        goTo("b")
                    }
                }

                finalState("b")
            }

            // when
            val stepsTaken = mutableListOf<Transition>()
            val completion = stateMachine.run { transitions ->
                transitions.first().also { stepsTaken.add(it) } to emptyMap()
            }
            val executableTransitions = stepsTaken.map { it.prepare(emptyMap()) }

            // then
            completion shouldBe Completion.FinalStateReached(
                data = emptyMap(),
                steps = executableTransitions,
            )
        }
    }
})