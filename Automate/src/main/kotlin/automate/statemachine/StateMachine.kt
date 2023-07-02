package automate.statemachine

import arrow.core.Either
import automate.data.ModelFeedback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

abstract class StateMachine<S : State<A>, Trans : Transition<S, A>, A>(
    initialState: S,
    val maxErrors: Int = 2,
    val maxSteps: Int = 100
) {
    private val internalState = MutableStateFlow(initialState)
    val state: StateFlow<S> = internalState

    private var steps = 0
    private var errors = 0
    private var feedback: List<ModelFeedback> = listOf()

    abstract fun availableTransitions(state: S): List<Trans>

    abstract suspend fun nextTransition(
        state: S,
        availableTransitions: List<Trans>,
        feedback: List<ModelFeedback>,
    ): Pair<Trans, InputMap>

    open suspend fun onFinished(state: S, feedback: List<ModelFeedback>) {}

    suspend fun run() {
        if (++steps >= maxSteps) {
            feedback += ModelFeedback.FatalError("Max steps reached! Reached $steps steps.")
            onFinished(state.value, feedback)
            return
        }
        if (state.value.isFinal) {
            onFinished(state.value, feedback)
            return
        }

        val (transition, input) = nextTransition(
            state = state.value,
            availableTransitions = availableTransitions(state.value),
            feedback = feedback,
        )
        when (
            val res = transition.transition(
                state = state.value,
                input = input
            )
        ) {
            is Either.Left -> {
                feedback += res.value
                if (++errors < maxErrors) {
                    run()
                } else {
                    feedback += ModelFeedback.FatalError("Max errors reached! Reached $errors errors.")
                    onFinished(state.value, feedback)
                    return
                }
            }

            is Either.Right -> {
                val (state, suggestions) = res.value
                feedback = feedback.map {
                    when (it) {
                        is ModelFeedback.Error -> it.copy(resolved = true)
                        else -> it
                    }
                } + suggestions
                internalState.value = state
                run()
            }
        }
    }
}