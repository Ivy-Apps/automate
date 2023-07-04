package automate.statemachine

import automate.statemachine.data.ModelFeedback.Suggestion
import java.time.LocalDateTime

@DslMarker
annotation class StateMachineDsl

class State

class StateMachine {
    fun run() {

    }
}

@StateMachineDsl
fun stateMachine(block: StateMachineScope.() -> Unit): StateMachine {
    TODO()
}

interface StateMachineScope {
    @StateMachineDsl
    fun initialState(name: String, block: StateScope.() -> Unit)

    @StateMachineDsl
    fun state(name: String, block: StateScope.() -> Unit)

    @StateMachineDsl
    fun finalState(name: String)
}

interface StateScope {
    val data: MutableMap<String, Any>

    @StateMachineDsl
    fun transition(
        name: String,
        inputs: InputScope.() -> Unit = {},
        transition: TransitionScope.() -> Pair<State, List<Suggestion>>
    )
}

interface InputScope {
    @StateMachineDsl
    fun input(name: String, description: String)
}

interface TransitionScope {
    @StateMachineDsl
    fun goTo(
        state: String,
        suggestions: SuggestionsScope.() -> Unit = { }
    ): Pair<State, List<Suggestion>>

    @StateMachineDsl
    fun error(error: String): Nothing

    @StateMachineDsl
    fun input(name: String): String
}

interface SuggestionsScope {
    @StateMachineDsl
    fun suggestion(feedback: String)
}

fun test() {
    val stateMachine = stateMachine {
        initialState("t&c") {
            transition("Agree") {
                data["agreement date"] = LocalDateTime.now()
                goTo("onboarding")
            }
        }

        state("onboarding") {
            transition("login", {
                input("email", "An email of an existing user.")
                input("password", "The password for that user.")
            }) {
                val email = input("email")
                val password = input("password")
                goTo("main") {
                    suggestion("Use stronger password.")
                }
            }

            transition("register", {
                input("email", "dfdfd")
                input("password", "fdsdsfsd")
            }) {
                goTo("main")
            }
        }

        finalState("main")
    }

    stateMachine.run()
}