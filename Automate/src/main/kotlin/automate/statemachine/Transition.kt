package automate.statemachine

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.ensureNotNull
import automate.data.ModelFeedback
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

typealias InputMap = Map<String, Any>

abstract class Transition<S : State<A>, A> {
    abstract val name: String
    open val description: String? = null

    abstract val input: List<TransitionParam<*>>
    abstract fun transition(
        state: S,
        input: InputMap
    ): Either<ModelFeedback.Error, Pair<S, List<ModelFeedback.Suggestion>>>

    @StateMachineDslMarker
    protected inline fun <reified T : Any> Raise<ModelFeedback.Error>.requiredParam(
        inputMap: InputMap,
        param: TransitionParam<T>
    ): T {
        val value = inputMap[param.name] as? T
        ensureNotNull(value) {
            ModelFeedback.Error(
                """
                Value for parameter "${param.name}" with type '${param.type.simpleName}'
                was not found in map: $inputMap.
                """.trimIndent()
            )
        }
        return value
    }
}

@Serializable
data class TransitionParam<T : Any>(
    val name: String,
    val type: KClass<T>,
    val description: String? = null,
)