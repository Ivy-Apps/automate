import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

class HelloTest : FreeSpec({
    "hello test" {
        "hello" shouldBe "hello"
    }
})