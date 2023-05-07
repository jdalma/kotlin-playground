import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@DisplayName("기본 테스트")
class BasicTest : FunSpec({

    test("더하기") {
        1 + 10 shouldBe 11
    }

    test("곱하기") {
        1.0 * 2.0 shouldBe 2.0
    }
})
