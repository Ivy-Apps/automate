package automate.statemachine

import automate.statemachine.impl.StateMachine.Completion
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

class StateMachineTest : FreeSpec({
    "terminates" - {
        "with max steps reached" {
            // given
            val stateMachine = stateMachine(maxErrors = 3) {
                initialState("test") {
                    transition("ok") {
                        goTo("test")
                    }
                }
            }

            // when
            val completion = stateMachine.run {
                TODO()
            }

            // then
            completion shouldBe Completion.MaxErrorsReached(listOf())
        }
    }
})