package _03_Function

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe

class FunctionTest: DescribeSpec({

    describe("더하기 함수는") {
        context("두 개의 정수를") {
            val pair = 1 to 10

            it("합산한다.") {
                Function.add(pair.first, pair.second) shouldBeEqual 11
                Function.add2(pair.first, pair.second) shouldBeEqual 11
                Function.add3(pair.first, pair.second) shouldBeEqual 11
                Function.add4(pair.first, pair.second) shouldBeEqual 11
                Function.add5 (pair.first, pair.second) shouldBeEqual 11

                (fun (x: Int, y: Int) = x + y) (pair.first, pair.second) shouldBeEqual 11
                { x: Int, y: Int -> x + y } (pair.first, pair.second) shouldBeEqual 11
            }
        }
    }

    describe("returnUnit 함수는") {
        context("Unit 을 반환한다.") {
            Function.returnUnit() shouldBeEqual Unit
        }
    }

    describe("nameShadowing") {
        Function.nameShadowingTest()
        Function.called shouldBeEqual 36
    }

    describe("고차함수의 클로저") {
        val func = Function.state(10)
        // 이 시점엔 Function.state가 끝났으므로 state 내부 지역 변수 var memory는 더 이상 스택에 존재하지 않는다.
        // 컴파일러는 반환된 람다를 나중에 호출해도 아무 문제가 없도록, 람다가 참조하는 람다 밖에 정의된 변숫값들을 포함한 데이터 구조를 힙에 저장한다.

        func(0) shouldBeEqual 10
        func(1) shouldBeEqual 10
        func(2) shouldBeEqual 1
        func(10) shouldBeEqual 2
        func(0) shouldBeEqual 10
    }

    describe("고차함수 예제") {
        val lambdaA = Function.a(1)
        val lambdaB = Function.b(1 , 2)
        val lambdaC = Function.c(1 , 2)
        val lambdaD = Function.d(1 , 2)
        val lambdaE = Function.e(1 , 2)
        val lambdaF = Function.f(1 , 2)

        lambdaA(3) shouldBeEqual 4
        lambdaB(3) shouldBeEqual 3
        lambdaC(3) shouldBeEqual 4
        lambdaD(3) shouldBeEqual 5
        lambdaE(3,4) shouldBeEqual 7
        lambdaF() shouldBeEqual 3
    }

    describe("joinToString 함수는") {

        context("구분자는 [,]를 사용하고 접두사는 [Start-], 접미사는 [-End]일 때") {
            val delimiter = ","
            val prefix = "Start-"
            val postfix = "-End"
            var array = arrayOf("첫 번째", "두 번째", "세 번째")

            it("문자열을 가공하여 반환한다.") {
                Function.joinToString(
                    array,
                    delimiter,
                    prefix,
                    postfix
                )  shouldBeEqual "Start-첫 번째,두 번째,세 번째-End"
            }
        }

        context("파라미터를 모두 전달하지 않는다면") {
            it("디폴트 파라미터가 사용된다.") {
                Function.joinToString() shouldBeEqual "Start-a,e,i,o,u-End"
            }
        }

        context("파라미터를 지정해준다면") {
            val prefix = "["
            val postfix = "]"
            it("지정된 파라미터만 적용한다.") {
                Function.joinToString(
                    prefix = prefix,
                    postfix = postfix
                ) shouldBeEqual "[a,e,i,o,u]"
            }
        }
    }

    describe("sumString 함수는") {
        context("문자열 파라미터를 여러 개 전달하면") {
            val a = "a"
            val b = "b"
            val c = "c"
            it("누적하여 반환한다.") {
                Function.sumString(a,b,c) shouldBeEqual "abc"
            }
        }

        context("문자열 배열을 한 번에 전달하면") {
            val strings = arrayOf("a","b","c")
            it("누적하여 반환한다.") {
                Function.sumString(*strings) shouldBeEqual "abc"
            }
        }
    }
})
