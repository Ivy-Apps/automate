package automate.statemachine

import arrow.core.Either

@DslMarker
annotation class StateMachineDsl

abstract class StateMachine<S : State<A>, Trn : Transition<S, A>, A>(
    initialState: S,
    private val maxErrors: Int = 3,
    private val maxSteps: Int = Int.MAX_VALUE
) {
    var state: S = initialState
        private set

    private var steps = 0
    private var errors = 0
    private var error: String? = null

    abstract fun availableTransitions(state: S): List<Trn>

    abstract suspend fun nextTransition(
        availableTransitions: List<Trn>,
        error: String?,
    ): Pair<Trn, InputMap>

    tailrec suspend fun run() {
        if (++steps >= maxSteps) {
            error = "Max steps reached! Reached $steps."
            return
        }
        if (state.isFinal) return

        val (transition, input) = nextTransition(
            availableTransitions = availableTransitions(state),
            error = error,
        )
        when (
            val res = transition.transition(
                state = state,
                input = input
            )
        ) {
            is Either.Left -> {
                error = res.value
                if (++errors < maxErrors) {
                    run()
                }
            }

            is Either.Right -> {
                error = null
                state = res.value
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