package automate.parser

@DslMarker
annotation class ParserDslMarker

fun <T> parse(text: String, parse: ParserDslScope.() -> T): T {
    return ParserImpl(text).run(parse)
}

interface ParserDslScope {
    @ParserDslMarker
    val position: Int

    @ParserDslMarker
    fun peek(): Char?

    @ParserDslMarker
    fun consume(): Char?

    @ParserDslMarker
    fun consumeString(string: String): String?

    @ParserDslMarker
    fun consumeUntil(endTag: String, dropEndTag: Boolean): String?

    @ParserDslMarker
    fun consumeWhitespace()
}