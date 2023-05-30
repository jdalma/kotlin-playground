package _09_Collection

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.equals.shouldNotBeEqual
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.yield
import kotlin.collections.Iterator

class Iterator : DescribeSpec ({

    data class Person (
        val name: String,
        val age: Int
    )

    val testA = Person("테스트A", 10)
    val testB = Person("테스트B", 15)
    val testC = Person("테스트C", 17)

    describe("Iterable 재정의") {

        class IterOneToFive: Iterable<Int> {
            override fun iterator(): Iterator<Int> = iterator {
                (1..5).forEach{ yield(it) }
            }
        }

        it("1부터 5까지의 숫자를 가진 Iterable 반환") {
            val iter = IterOneToFive()

            iter.min() shouldBe 1
            iter.max() shouldBe 5
        }

        it("표준 라이브러리에 있는 함수인 Iterable()") {
            val iterable = Iterable {
                iterator {
                    (1..5).forEach{ yield(it) }
                }
            }
            var number = 1
            for (i in iterable) {
                i shouldBe number++
            }
        }
    }

    describe("Iterator") {

        val iter = listOf(
            testA,
            testB,
            testC
        ).iterator() // kotlin.collections.Iterator

        it("블록 내부에서 원소를 제공할 수 있다.") {
            val yieldIter = iterator { // SequenceBuilderIterator
                // 빌드 중인 Iterator 에 값을 생성하고 다음 값이 요청될 때까지 일시 중단합니다.
                yield(testA)
                yield(testB)
                yield(testC)
            }

            while (iter.hasNext()) {
                iter.next() shouldBeEqual yieldIter.next()
            }
            iter shouldNotBeEqual yieldIter
        }
    }

})
