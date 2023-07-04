package automate.statemachine.impl

import automate.statemachine.InputScope
import automate.statemachine.TransitionScope
import automate.statemachine.data.Feedback

typealias InputsMap = MutableMap<String, String>
typealias TransitionFun = suspend (InputsMap) -> Pair<String, List<Feedback.Warning>>

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
    private val name: String,
    private val inputsMap: InputsMap,
    private val transitionFun: TransitionFun,
) {
    suspend fun execute(): Pair<String, List<Feedback.Warning>> {
        return transitionFun(inputsMap)
    }
}

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
) : TransitionScope {

    override fun goTo(
        state: String,
        feedback: List<String>
    ): Pair<String, List<Feedback.Warning>> {
        return state to feedback.map(Feedback::Warning)
    }

    @Throws(Error::class)
    override fun error(error: String): Nothing {
        throw Error(error)
    }

    override fun input(name: String): String {
        return inputs[name] ?: error("Missing input '$name'.")
    }

    data class Error(val error: String) : Exception()
}

