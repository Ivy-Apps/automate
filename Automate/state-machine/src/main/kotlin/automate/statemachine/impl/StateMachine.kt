package automate.statemachine.impl

import automate.statemachine.StateMachineDsl
import automate.statemachine.StateMachineScope
import automate.statemachine.StateScope

class StateMachine(
    private val maxSteps: Int,
    private val maxErrors: Int,
) : StateMachineScope {
    var steps = 0
        private set
    var errors = 0
        private set

    val states: MutableMap<String, State> = mutableMapOf()

    override val data: MutableMap<String, Any> = mutableMapOf()

    suspend fun run(
        nextTransition: suspend StateMachineTransitionScope.(List<Transition>) -> ExecutableTransition
    ) {

    }

    override fun initialState(name: String, block: StateScope.() -> Unit) {
        TODO("Not yet implemented")
    }

    override fun state(name: String, block: StateScope.() -> Unit) {
        TODO("Not yet implemented")
    }

    override fun finalState(name: String) {
        TODO("Not yet implemented")
    }
}

interface StateMachineTransitionScope {
    @StateMachineDsl
    fun error(error: String): Nothing
}