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

    describe("고차함수의 Name Shadowing") {
        val func = Function.state(10)
        // 이 시점엔 Function.state가 끝났으므로 state 내부 지역 변수 var memory는 더 이상 스택에 존재하지 않는다.
        // 컴파일러는 반환된 람다를 나중에 호출해도 아무 문제가 없도록, 람다가 참조하는 람다 밖에 정의된 변숫값들을 포함한 데이터 구조를 힙에 저장한다.

        func(0) shouldBe 10
        func(1) shouldBe 10
        func(2) shouldBe 1
        func(10) shouldBe 2
        func(0) shouldBe 10
    }
})
