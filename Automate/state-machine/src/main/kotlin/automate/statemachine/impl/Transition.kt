package automate.statemachine.impl

import automate.statemachine.InputScope
import automate.statemachine.NonFatalFeedbackScope
import automate.statemachine.TransitionScope
import automate.statemachine.data.Feedback

typealias InputsMap = MutableMap<String, String>
typealias TransitionFun = (InputsMap) -> Pair<String, List<Feedback.Warning>>

data class Transition(
    val name: String,
    val inputs: List<InputDef>,
    private val transitionFun: TransitionFun,
) {
    fun execute(inputsMap: InputsMap): ExecutableTransition {
        return ExecutableTransition(
            name = name,
            inputsMap = inputsMap,
            transitionFun = transitionFun,
        )
    }
}

data class ExecutableTransition(
    val name: String,
    val inputsMap: InputsMap,
    val transitionFun: TransitionFun,
)

data class InputDef(
    val name: String,
    val description: String,
)

class InputScopeImpl : InputScope {
    private val _inputs = mutableListOf<InputDef>()
    val inputs: List<InputDef> = _inputs

    override fun input(name: String, description: String) {
        _inputs.add(InputDef(name = name, description = description))
    }
}

class TransitionScopeImpl(
    private val inputs: InputsMap
) : TransitionScope, NonFatalFeedbackScope {
    private val nonFatalFeedback: MutableList<Feedback.Warning> = mutableListOf()

    override fun goTo(
        state: String,
        suggestions: NonFatalFeedbackScope.() -> Unit
    ): Pair<String, List<Feedback.Warning>> {
        return state to nonFatalFeedback
    }

    @Throws(TransitionError::class)
    override fun error(error: String): Nothing {
        throw TransitionError(error)
    }

    override fun input(name: String): String {
        return inputs[name] ?: error("Missing input '$name'.")
    }

    override fun warning(feedback: String) {
        nonFatalFeedback.add(Feedback.Warning(feedback))
    }
}

data class TransitionError(val error: String) : Exception()
