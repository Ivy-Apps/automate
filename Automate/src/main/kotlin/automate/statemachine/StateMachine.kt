package automate.statemachine

import arrow.core.Either
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@DslMarker
annotation class StateMachineDsl

abstract class StateMachine<S : State<A>, Trans : Transition<S, A>, A>(
    initialState: S,
    private val maxErrors: Int = 3,
    private val maxSteps: Int = Int.MAX_VALUE
) {
    private val internalState = MutableStateFlow(initialState)
    val state: StateFlow<S> = internalState

    private var steps = 0
    private var errors = 0
    private var error: String? = null

    abstract fun availableTransitions(state: S): List<Trans>

    abstract suspend fun nextTransition(
        availableTransitions: List<Trans>,
        error: String?,
    ): Pair<Trans, InputMap>

    open suspend fun onFinished(state: S, error: String?) {}

    tailrec suspend fun run() {
        if (++steps >= maxSteps) {
            error = "Max steps reached! Reached $steps."
            onFinished(state.value, error)
            return
        }
        if (state.value.isFinal) {
            onFinished(state.value, error)
            return
        }

        val (transition, input) = nextTransition(
            availableTransitions = availableTransitions(state.value),
            error = error,
        )
        when (
            val res = transition.transition(
                state = state.value,
                input = input
            )
        ) {
            is Either.Left -> {
                error = res.value
                if (++errors < maxErrors) {
                    run()
                } else {
                    onFinished(state.value, error)
                    return
                }
            }

            is Either.Right -> {
                error = null
                internalState.value = res.value
                run()
            }
        }
    }
}

interface State<A> {
    val data: A
    val isFinal: Boolean
        get() = false
}