package automate.statemachine.impl

import automate.statemachine.StateMachineDsl
import automate.statemachine.StateMachineScope
import automate.statemachine.StateScope
import automate.statemachine.data.Feedback

class StateMachine(
    private val maxSteps: Int,
    private val maxErrors: Int,
) : StateMachineScope {
    private val steps = mutableListOf<ExecutableTransition>()
    private val errors = mutableListOf<Feedback.Error>()

    private var _currentState: State? = null
    val currentState: State
        get() = _currentState ?: error("No initial state.")

    private val _states = mutableMapOf<String, State>()
    val states: Map<String, State> = _states

    private val _data = mutableMapOf<String, Any>()
    val data: Map<String, Any> = _data

    suspend fun run(
        nextTransition: suspend StateMachineTransitionScope.(List<Transition>) -> ExecutableTransition
    ): Completion {
        if (states.size >= maxSteps) {
            return Completion.MaxStepsReached(steps)
        }
        if (errors.size >= maxErrors) {
            return Completion.MaxErrorsReached(errors)
        }

        TODO()
    }


    override fun initialState(name: String, block: StateScope.() -> Unit) {
        _currentState = createState(name, block).copy(
            special = SpecialState.Initial
        ).also {
            _states[name] = it
        }
    }

    override fun state(name: String, block: StateScope.() -> Unit) {
        _states[name] = createState(name, block)
    }

    override fun finalState(name: String) {
        _states[name] = createState(name, block = {}).copy(
            special = SpecialState.Final
        )
    }

    private fun createState(name: String, block: StateScope.() -> Unit): State {
        return State(
            name = name,
            data = _data
        ).apply(block)
    }


    sealed interface Completion {
        data class Success(val data: Map<String, Any>) : Completion
        data class MaxErrorsReached(val errors: List<Feedback.Error>) : Completion
        data class MaxStepsReached(val transitions: List<ExecutableTransition>) : Completion
    }

    private fun error(error: String): Nothing {
        throw StateMachineError(error)
    }
}

interface StateMachineTransitionScope {
    @StateMachineDsl
    fun error(error: String): Nothing
}

data class StateMachineError(val error: String) : Exception(error)