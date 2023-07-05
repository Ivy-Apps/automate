package automate.statemachine.impl

import automate.statemachine.StateMachineDsl
import automate.statemachine.StateMachineScope
import automate.statemachine.StateScope
import automate.statemachine.data.StateMachineError

class StateMachine internal constructor(
    private val maxSteps: Int,
    private val maxErrors: Int,
) : StateMachineScope {
    private val _steps = mutableListOf<ExecutableTransition>()
    val steps: List<ExecutableTransition> = _steps

    private val _errors = mutableListOf<StateMachineError>()
    val errors: List<StateMachineError> = _errors

    private var _currentState: State? = null
    val currentState: State
        get() = _currentState ?: error("No initial state.")

    private val _states = mutableMapOf<String, State>()
    val states: Map<String, State> = _states

    private val _data = mutableMapOf<String, Any>()
    val data: Map<String, Any> = _data

    suspend fun run(
        nextTransition: suspend NextTransitionScope.(
            availableTransitions: Map<String, Transition>
        ) -> Pair<Transition, InputsMap>
    ): Completion {
        if (states.size >= maxSteps) {
            return Completion.MaxStepsReached(_steps)
        }
        if (_errors.size >= maxErrors) {
            return Completion.MaxErrorsReached(_errors)
        }
        if (currentState.isFinal) {
            return Completion.Success(data, _steps)
        }

        return executeTransition(nextTransition)
    }

    private suspend fun executeTransition(
        nextTransition: suspend NextTransitionScope.(Map<String, Transition>) -> Pair<Transition, InputsMap>
    ): Completion {
        try {
            val scope = NextTransitionScopeImpl(
                error = _errors.lastOrNull(),
            )
            val (transition, inputs) = nextTransition(scope, currentState.transitions)
            val executableTransition = transition.prepare(inputs)
            val nextStateName = executableTransition.execute().state
            val nextState = states[nextStateName] ?: throw TransitionScopeImpl.Error(
                "Invalid next state '$nextStateName'."
            )
            _steps.add(executableTransition)
            _currentState = nextState
        } catch (e: NextTransitionScopeImpl.Error) {
            _errors.add(StateMachineError.LogicError(e.error))
        } catch (e: TransitionScopeImpl.Error) {
            _errors.add(StateMachineError.TransitionError(e.error))
        }
        return run(nextTransition)
    }

    override fun initialState(name: String, block: StateScope.() -> Unit) {
        _currentState = createState(name, block).also {
            _states[name] = it
        }
    }

    override fun state(name: String, block: StateScope.() -> Unit) {
        _states[name] = createState(name, block)
    }

    override fun finalState(name: String) {
        _states[name] = createState(name, block = {}).copy(
            isFinal = true,
        )
    }

    private fun createState(name: String, block: StateScope.() -> Unit): State {
        return State(
            name = name,
            data = _data
        ).apply(block)
    }

    sealed interface Completion {
        data class Success(
            val data: Map<String, Any>,
            val steps: List<ExecutableTransition>
        ) : Completion

        data class MaxErrorsReached(val errors: List<StateMachineError>) : Completion

        data class MaxStepsReached(val steps: List<ExecutableTransition>) : Completion
    }

    @Throws(StateMachineBuilderException::class)
    private fun error(error: String): Nothing {
        throw StateMachineBuilderException(error)
    }
}

class NextTransitionScopeImpl(
    override val error: StateMachineError?,
) : NextTransitionScope {
    @Throws(Error::class)
    override fun error(error: String): Nothing {
        throw Error(error)
    }

    data class Error(val error: String) : Exception()
}

interface NextTransitionScope {
    val error: StateMachineError?

    @StateMachineDsl
    fun error(error: String): Nothing
}

data class StateMachineBuilderException(val error: String) : Exception(error)