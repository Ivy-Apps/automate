package automate.statemachine

import automate.statemachine.impl.NextState
import automate.statemachine.impl.StateMachine

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
        description: String? = null,
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