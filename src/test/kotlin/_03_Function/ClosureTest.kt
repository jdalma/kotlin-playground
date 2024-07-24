package _03_Function

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.equals.shouldBeEqual

class ClosureTest: StringSpec ({

    "고차함수의 클로저" {
        val func = Function.state(10)
        // 이 시점엔 Function.state가 끝났으므로 state 내부 지역 변수 var memory는 더 이상 스택에 존재하지 않는다.
        // 컴파일러는 반환된 람다를 나중에 호출해도 아무 문제가 없도록, 람다가 참조하는 람다 밖에 정의된 변숫값들을 포함한 데이터 구조를 힙에 저장한다.

        func(0) shouldBeEqual 10
        func(1) shouldBeEqual 10
        func(2) shouldBeEqual 1
        func(10) shouldBeEqual 2
        func(0) shouldBeEqual 10

        fun counter(): () -> Int {
            var i = 0
            return { i++ }
        }

        val next = counter()
        next() shouldBeEqual 0
        next() shouldBeEqual 1
        next() shouldBeEqual 2

        val next2 = counter()
        next2() shouldBeEqual 0
        next2() shouldBeEqual 1
        next2() shouldBeEqual 2
    }

    "클로저를 활용한 메모이제이션" {
        var sumExecuteCount = 0
        fun sum(numbers: Set<Int>): Int {
            sumExecuteCount++
            return numbers.sum()
        }
        fun summarizer(): (Set<Int>) -> Int {
            val resultCache = mutableMapOf<Set<Int>, Int>()

            return { numbers: Set<Int> ->
                resultCache.computeIfAbsent(numbers, ::sum)
            }
        }

        val input = listOf(
            setOf(1, 2, 3),
            setOf(3, 1, 2),
            setOf(3, 2, 1)
        )
        val summarizer = summarizer()

        for (set in input) {
            summarizer(set) shouldBeEqual 6
        }
        sumExecuteCount shouldBeEqual 1
    }

})
