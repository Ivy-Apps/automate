package automate.statemachine.impl

import automate.statemachine.InputScope
import automate.statemachine.TransitionScope

@JvmInline
value class NextState(val state: String)

typealias InputsMap = Map<String, String>
typealias TransitionFun = suspend (InputsMap) -> NextState

data class Transition(
    val name: String,
    val description: String?,
    val inputs: List<InputDef>,
    private val transitionFun: TransitionFun,
) {
    internal fun prepare(inputsMap: InputsMap): ExecutableTransition {
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
    suspend fun execute(): NextState {
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
        _inputs.add(InputDef(name = name.uppercase(), description = description))
    }
}

class TransitionScopeImpl(
    private val inputs: InputsMap
) : TransitionScope {

    override fun goTo(state: String): NextState {
        return NextState(state)
    }

    @Throws(Error::class)
    override fun error(error: String): Nothing {
        throw Error(error)
    }

    override fun input(name: String): String {
        return inputs[name.uppercase()] ?: error("Missing input '${name.uppercase()}'.")
    }

    data class Error(val error: String) : Exception()
}

