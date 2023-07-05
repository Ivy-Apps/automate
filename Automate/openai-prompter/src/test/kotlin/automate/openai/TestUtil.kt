package automate.openai

import automate.statemachine.impl.InputDef
import automate.statemachine.impl.Transition

fun transition(
    name: String,
    description: String? = null,
    inputs: List<InputDef> = emptyList(),
) = Transition(
    name = name,
    description = description,
    inputs = inputs,
    transitionFun = { error("") }
)

fun log(thing: String) {
    println("----------------")
    println(thing)
    println("----------------")
}