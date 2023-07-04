package automate.statemachine.impl

import automate.statemachine.InputScope
import automate.statemachine.StateScope
import automate.statemachine.TransitionScope
import automate.statemachine.data.Feedback

data class State(
    val name: String,
    override val data: MutableMap<String, Any>,
    val isFinal: Boolean = false,
) : StateScope {
    private val _transitions = mutableMapOf<String, Transition>()
    val transitions: Map<String, Transition> = _transitions

    override fun transition(
        name: String,
        inputs: InputScope.() -> Unit,
        transition: TransitionScope.() -> Pair<String, List<Feedback.Warning>>
    ) {
        val inputScope = InputScopeImpl()
        inputScope.inputs()
        _transitions[name] = Transition(
            name = name,
            inputs = inputScope.inputs,
            transitionFun = { inputsMap ->
                TransitionScopeImpl(inputsMap).transition()
            }
        )
    }
}