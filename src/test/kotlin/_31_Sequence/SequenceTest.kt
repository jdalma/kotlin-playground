package _31_Sequence

import io.kotest.common.runBlocking
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.equals.shouldBeEqual
import kotlinx.coroutines.coroutineScope
import kotlin.system.measureTimeMillis

class SequenceTest: StringSpec ({

    "집합 자료 구조와 sequence의 성능 차이" {
        val numbers = (1..1_000_000).toList()

        println(measureTimeMillis {
            numbers.map { it * it }.take(1).count() shouldBeEqual 1
        }) // ~50ms

        println(measureTimeMillis {
            numbers.asSequence().map { it * it }.take(1).count() shouldBeEqual 1
        }) // ~5ms

        println(measureTimeMillis {
            numbers.map { it * it }.count() shouldBeEqual 1_000_000
        }) // ~50ms

        println(measureTimeMillis {
            numbers.asSequence().map { it * it }.count() shouldBeEqual 1_000_000
        }) // ~5ms
    }

    "generateSequence" {
        val seq1: Sequence<Long> = generateSequence(1L) { it + 1 }
        val seq2 = (1 .. 100).asSequence()
        val seq3 = sequence {
            var a = 0
            var b = 1
            yield(a)
            yield(b)
            while (true) {
                yield(a + b)
                val t = a
                a = b
                b += t
            }
        }
    }

    "sequence 필요한 만큼 연산하기" {
        val numbers = sequence {
            println("1 생성")
            yield(1)
            println("3, 5 생성")
            yieldAll(listOf(3, 5))
            println("나머지 생성")
            yieldAll(generateSequence(7) { it + 3 })
        }
        numbers.take(1).toList() shouldBeEqual listOf(1)
        numbers.take(4).toList() shouldBeEqual listOf(1, 3, 5, 7)
        numbers.take(10).toList() shouldBeEqual listOf(1, 3, 5, 7, 10, 13, 16, 19, 22, 25)
    }
})
