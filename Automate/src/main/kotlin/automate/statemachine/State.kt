package automate.statemachine

interface State<A> {
    val data: A
    val isFinal: Boolean
        get() = false
}