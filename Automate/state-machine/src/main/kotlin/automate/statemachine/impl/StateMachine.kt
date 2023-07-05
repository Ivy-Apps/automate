package automate.statemachine.impl

import arrow.core.NonEmptySet
import arrow.core.toNonEmptySetOrNull
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
        transitionProvider: TransitionProvider
    ): Completion {
        if (steps.size >= maxSteps) {
            return Completion.MaxStepsReached(_steps)
        }
        if (_errors.size >= maxErrors) {
            return Completion.MaxErrorsReached(_errors)
        }
        if (currentState.isFinal) {
            return Completion.FinalStateReached(data, _steps)
        }

        return executeTransition(transitionProvider)
    }

    private suspend fun executeTransition(transitionProvider: TransitionProvider): Completion {
        try {
            val scope = NextTransitionProviderScopeImpl(
                error = _errors.lastOrNull(),
            )
            val (transition, inputs) = with(transitionProvider) {
                scope.provideNextTransition(availableTransitions())
            }
            val executableTransition = transition.prepare(inputs)
            val nextStateName = executableTransition.execute().state
            val nextState = states[nextStateName] ?: throw TransitionScopeImpl.Error(
                "Invalid next state '$nextStateName'."
            )
            _steps.add(executableTransition)
            _currentState = nextState
        } catch (e: NextTransitionProviderScopeImpl.Error) {
            _errors.add(StateMachineError.TransitionProvider(e.error))
        } catch (e: TransitionScopeImpl.Error) {
            _errors.add(StateMachineError.Transition(e.error))
        }
        return run(transitionProvider)
    }

    private fun availableTransitions(): NonEmptySet<Transition> {
        return currentState.transitions.values.toNonEmptySetOrNull()
            ?: error("State '${currentState.name}' doesn't have defined transitions.")
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
        data class FinalStateReached(
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

fun interface TransitionProvider {
    suspend fun NextTransitionProviderScope.provideNextTransition(
        availableTransitions: NonEmptySet<Transition>
    ): Pair<Transition, InputsMap>
}

class NextTransitionProviderScopeImpl(
    override val error: StateMachineError?,
) : NextTransitionProviderScope {
    @Throws(Error::class)
    override fun error(error: String): Nothing {
        throw Error(error)
    }

    data class Error(val error: String) : Exception()
}

interface NextTransitionProviderScope {
    val error: StateMachineError?

    @StateMachineDsl
    fun error(error: String): Nothing
}

data class StateMachineBuilderException(val error: String) : Exception(error)