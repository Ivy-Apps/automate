package automate.statemachine

import arrow.core.Either
import automate.data.ModelFeedback
import automate.openai.chatgpt.ChatGptPrompter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

abstract class StateMachine<S : State<A>, Trans : Transition<S, A>, A : Any>(
    initialState: S,
    val maxErrors: Int = 2,
    val maxSteps: Int = 100
) {
    private val internalState = MutableStateFlow(initialState)
    val state: StateFlow<S> = internalState

    var steps = 0
        private set
    var errors = 0
        private set
    private var feedback: List<ModelFeedback> = listOf()

    abstract fun availableTransitions(state: S): List<Trans>

    abstract val prompter: ChatGptPrompter<A, S, Trans>

    private suspend fun nextTransition(
        state: S,
        availableTransitions: List<Trans>,
        feedback: List<ModelFeedback>,
    ): Either<ModelFeedback.Error, Pair<Trans, InputMap>> {
        return prompter.prompt(
            state = state,
            feedback = feedback,
            availableTransition = availableTransitions,
            steps = steps,
            maxSteps = maxSteps,
        )
    }

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

        val next = nextTransition(
            state = state.value,
            availableTransitions = availableTransitions(state.value),
            feedback = feedback,
        )
        if (next is Either.Left) {
            feedback += next.value
            run()
            return
        }
        val (transition, input) = (next as Either.Right).value

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
                    return
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