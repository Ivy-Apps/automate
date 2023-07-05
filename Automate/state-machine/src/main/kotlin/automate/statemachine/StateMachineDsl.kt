package automate.statemachine

import automate.statemachine.impl.NextState
import automate.statemachine.impl.StateMachine
import java.time.LocalDateTime

@DslMarker
annotation class StateMachineDsl


@StateMachineDsl
fun stateMachine(
    maxSteps: Int = 30,
    maxErrors: Int = 10,
    block: StateMachineScope.() -> Unit
): StateMachine {
    return StateMachine(
        maxSteps = maxSteps,
        maxErrors = maxErrors,
    ).also(block)
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
    @StateMachineDsl
    val data: MutableMap<String, Any>

    @StateMachineDsl
    fun transition(
        name: String,
        inputs: InputScope.() -> Unit = {},
        transition: TransitionScope.() -> NextState
    )
}

interface InputScope {
    @StateMachineDsl
    fun input(name: String, description: String)
}

interface TransitionScope {
    @StateMachineDsl
    fun goTo(state: String): NextState

    @StateMachineDsl
    fun error(error: String): Nothing

    @StateMachineDsl
    fun input(name: String): String
}

suspend fun test() {
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
                data["auth-token"] = "ok"
                goTo("main")
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

    stateMachine.run { transitions ->
        val transition = transitions.first()
        transition to emptyMap()
    }
}