package automate.statemachine.prompt

import arrow.core.Either
import automate.data.ModelFeedback
import automate.statemachine.InputMap
import automate.statemachine.State
import automate.statemachine.Transition

interface StateMachinePrompter<A : Any, S : State<A>, Trans : Transition<S, A>> {
    suspend fun prompt(
        state: S,
        feedback: List<ModelFeedback>,
        availableTransitions: List<Trans>,
        steps: Int,
        maxSteps: Int,
    ): Either<ModelFeedback.Error, Pair<Trans, InputMap>>
}