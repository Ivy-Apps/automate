package automate.parser

class ParserImpl(
    private val text: String,
) : ParserDslScope {
    override var position: Int = 0
        private set

    override fun peek(): Char? {
        return text.getOrNull(position)
    }

    override fun consume(): Char? {
        return text.getOrNull(position++)
    }

    override fun consumeString(string: String): String? {
        return if (text.startsWith(prefix = string, startIndex = position)) {
            position += string.length
            string
        } else null
    }

    override fun consumeUntil(endTag: String, dropEndTag: Boolean): String? {
        return buildString {
            while (true) {
                val char = consume() ?: break
                append(char)
                if (this.toString().endsWith(endTag)) {
                    break
                }
            }
        }.takeIf { it.isNotEmpty() }?.let {
            if (dropEndTag && it.endsWith(endTag)) {
                it.dropLast(endTag.length)
            } else it
        }
    }

    override fun consumeWhitespace() {
        while (peek()?.isWhitespace() == true) {
            consume()
        }
    }

    override fun move(n: Int) {
        position += n
    }
}