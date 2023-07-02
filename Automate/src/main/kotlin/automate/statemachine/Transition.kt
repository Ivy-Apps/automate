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
                Value for parameter '${param.name}' not found in the "input" map.
                """.trimIndent()
            )
        }
        return value
    }

    @StateMachineDslMarker
    protected inline fun <reified T : Any> Raise<ModelFeedback.Error>.optionalParamø(
        inputMap: InputMap,
        param: TransitionParam<T>
    ): T? = inputMap[param.name] as? T
}

@Serializable
data class TransitionParam<T : Any>(
    val name: String,
    val type: KClass<T>,
    val description: String? = null,
    val tips: List<String>? = null,
    val optional: Boolean = false,
)