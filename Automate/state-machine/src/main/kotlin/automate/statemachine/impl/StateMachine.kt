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

    private var feedback = listOf<Feedback.Warning>()

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
            return Completion.MaxStepsReached(steps)
        }
        if (errors.size >= maxErrors) {
            return Completion.MaxErrorsReached(errors)
        }
        if (currentState.isFinal) {
            return Completion.Success(data, steps)
        }

        return executeTransition(nextTransition)
    }

    private suspend fun executeTransition(
        nextTransition: suspend NextTransitionScope.(Map<String, Transition>) -> Pair<Transition, InputsMap>
    ): Completion {
        try {
            val scope = NextTransitionScopeImpl(
                error = errors.lastOrNull(),
                feedback = feedback
            )
            val (transition, inputs) = nextTransition(scope, currentState.transitions)
            val (nextStateName, feedback) = transition.prepare(inputs)
                .also { steps.add(it) }
                .execute()
            val nextState = states[nextStateName] ?: error("Invalid next state '$nextStateName'.")
            this.feedback = feedback
            _currentState = nextState
        } catch (e: NextTransitionScopeImpl.Error) {
            errors.add(Feedback.Error(e.error))
        } catch (e: TransitionScopeImpl.Error) {
            errors.add(Feedback.Error(e.error))
        } catch (e: StateMachineError) {
            errors.add(Feedback.Error(e.error))
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

        data class MaxErrorsReached(val errors: List<Feedback.Error>) : Completion
        data class MaxStepsReached(val steps: List<ExecutableTransition>) : Completion
    }

    @Throws(StateMachineError::class)
    private fun error(error: String): Nothing {
        throw StateMachineError(error)
    }
}

class NextTransitionScopeImpl(
    override val error: Feedback.Error?,
    override val feedback: List<Feedback.Warning>,
) : NextTransitionScope {
    @Throws(Error::class)
    override fun error(error: String): Nothing {
        throw Error(error)
    }

    data class Error(val error: String) : Exception()
}

interface NextTransitionScope {
    val error: Feedback.Error?
    val feedback: List<Feedback.Warning>

    @StateMachineDsl
    fun error(error: String): Nothing
}

data class StateMachineError(val error: String) : Exception(error)