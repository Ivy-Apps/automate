package automate.statemachine

import io.kotest.core.spec.style.FreeSpec

class StateMachineTest : FreeSpec({
    "terminates" - {
        "with max steps reached" {
            // given
            val stateMachine = stateMachine(maxErrors = 3) {

            }
        }
    }
})