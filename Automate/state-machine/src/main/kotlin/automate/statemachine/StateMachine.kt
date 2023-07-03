package automate.statemachine

import arrow.core.Either
import automate.data.ModelFeedback
import automate.logger
import automate.statemachine.prompt.StateMachinePrompter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

abstract class StateMachine<S : State<A>, Trans : Transition<S, A>, A : Any>(
    initialState: S,
    val maxActiveErrors: Int = 2,
    val maxErrors: Int = 10,
    val maxSteps: Int = 30
) {
    private val internalState = MutableStateFlow(initialState)
    val state: StateFlow<S> = internalState

    var steps = 0
        private set
    var errorsOccurred = 0
        private set
    var feedback: List<ModelFeedback> = listOf()
        private set

    abstract fun availableTransitions(state: S): List<Trans>

    abstract val prompter: StateMachinePrompter<A, S, Trans>

    private suspend fun nextTransition(
        state: S,
        availableTransitions: List<Trans>,
        feedback: List<ModelFeedback>,
    ): Either<ModelFeedback.Error, Pair<Trans, InputMap>> {
        return prompter.prompt(
            state = state,
            feedback = feedback,
            availableTransitions = availableTransitions,
            steps = steps,
            maxSteps = maxSteps,
        )
    }

    open suspend fun onFinished(state: S, feedback: List<ModelFeedback>) {}

    suspend fun run() {
        if (++steps >= maxSteps) {
            val msg = "Max steps reached! Reached $steps steps."
            logger.info(msg)
            feedback += ModelFeedback.FatalError(msg)
            onFinished(state.value, feedback)
            return
        }
        if (errorsOccurred >= maxErrors) {
            val msg = "Max errors reached! Reached $errorsOccurred errors."
            logger.error(msg)
            feedback += ModelFeedback.FatalError(msg)
            onFinished(state.value, feedback)
            return
        }
        if (activeErrors() >= maxActiveErrors) {
            val msg = "Max active errors reached! Reached $maxActiveErrors not resolved errors."
            logger.error(msg)
            feedback += ModelFeedback.FatalError(msg)
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
            errorsOccurred++
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
                errorsOccurred++
                run()
                return
            }

            is Either.Right -> {
                val (state, suggestions) = res.value
                feedback = feedback.filterIsInstance(
                    ModelFeedback.Suggestion::class.java
                ) + suggestions
                internalState.value = state
                run()
                return
            }
        }
    }

    fun activeErrors(): Int = feedback.count { it is ModelFeedback.Error }
}