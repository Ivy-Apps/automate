package automate.statemachine

interface State<A> {
    val data: A
    val expectedOutcome: String
    val isFinal: Boolean
        get() = false
}