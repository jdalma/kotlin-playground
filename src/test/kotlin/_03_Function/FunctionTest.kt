package _03_Function

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class FunctionTest: DescribeSpec({

    describe("더하기 함수는") {
        context("두 개의 정수를") {
            val pair = 1 to 10

            it("합산한다.") {
                Function.add(pair.first, pair.second) shouldBe 11
                Function.add2(pair.first, pair.second) shouldBe 11
                Function.add3(pair.first, pair.second) shouldBe 11
                Function.add4(pair.first, pair.second) shouldBe 11
                Function.add5 (pair.first, pair.second) shouldBe 11

                (fun (x: Int, y: Int) = x + y) (pair.first, pair.second) shouldBe 11
                { x: Int, y: Int -> x + y } (pair.first, pair.second) shouldBe 11
            }
        }
    }

    describe("returnUnit 함수는") {
        context("Unit 을 반환한다.") {
            Function.returnUnit() shouldBe Unit
        }
    }

    describe("nameShadowing") {
        Function.nameShadowingTest()
        Function.called shouldBe 36
    }

})
