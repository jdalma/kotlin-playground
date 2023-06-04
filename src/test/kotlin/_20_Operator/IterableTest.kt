package _20_Operator

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe

class IterableTest: BehaviorSpec ({

    val size = 5
    val sum = 15

    given("Iterable과 Iterator를 implement한 클래스는") {
        class ImplementIterable(private val size: Int): Iterable<Int> {
            override fun iterator(): Iterator<Int> = ImplementIterator(size)

            inner class ImplementIterator(private val size: Int) : Iterator<Int> {
                var number: Int = 0
                override fun hasNext(): Boolean = number++ < size
                override fun next(): Int = number
            }
        }


        `when`("Iterable,Iterator 둘 다 for 문을 사용할 수 있다.") {

            then("for .. in") {
                val iterable = ImplementIterable(size)
                val iterator = iterable.iterator()

                var test = 0
                for (i in iterable) { test += i }

                /**
                 * iterable을 통해 for문을 실행하면 내부 iterator의 number 상태가 변경되어있을 줄 알았지만, 0 그대로다.
                 * iterable의 for문을 사용해도 내부 iterator는 일회용으로 사용되는 것 같다.
                 */
                test shouldBe sum
                iterable.iterator().next() shouldBe 0

                var test2 = 0
                for (i in iterator) { test2 += i }
                test2 shouldBe sum

                test shouldBeEqual test2
            }

            then("forEach 블록") {
                val iterable = ImplementIterable(size)
                val iterator = iterable.iterator()

                var test = 0
                iterable.forEach { test += it }
                test shouldBe sum

                var test2 = 0
                iterator.forEach { test2 += it }
                test2 shouldBe sum
            }

            then("Iterable만 sum()이 존재한다.") {
                val iterable = ImplementIterable(size)

                iterable.sum() shouldBe sum
            }
        }
    }

    given("Iterable과 Iterator를 implement하지않고 operator만 작성한 클래스는") {
        class JustIterable(private val size: Int) {
            operator fun iterator(): JustIterator = JustIterator(size)

            inner class JustIterator(private val size: Int) {
                private var number: Int = 0
                operator fun hasNext(): Boolean = number++ < size
                operator fun next(): Int = number
            }
        }

        `when`("Iterable만 for 문을 사용할 수 있다.") {
            val iterable = JustIterable(size)
            val iterator = iterable.iterator()

            then("for .. in") {
                var test = 0
                for (i in iterable) { test += i }
                test shouldBe sum
            }

            then("컴파일 에러") {
//                for (i in iterator) { }
//                iterable.forEach { test += it }
//                iterator.forEach { test += it }
//                iterable.sum()
//                iterator.sum()
            }
        }
    }
})
