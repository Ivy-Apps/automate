package automate.statemachine.impl

import automate.statemachine.InputScope
import automate.statemachine.StateScope
import automate.statemachine.TransitionScope

data class State(
    val name: String,
    override val data: MutableMap<String, Any>,
    val isFinal: Boolean = false,
) : StateScope {
    private val _transitions = mutableMapOf<String, Transition>()
    val transitions: Map<String, Transition> = _transitions

    override fun transition(
        name: String,
        description: String?,
        inputs: InputScope.() -> Unit,
        transition: TransitionScope.() -> NextState
    ) {
        val inputScope = InputScopeImpl()
        inputScope.inputs()
        _transitions[name] = Transition(
            name = name,
            description = description,
            inputs = inputScope.inputs,
            transitionFun = { inputsMap ->
                TransitionScopeImpl(inputsMap).transition()
            }
        )
    }
}