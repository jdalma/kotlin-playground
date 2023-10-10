package kotlinfp

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.equals.shouldBeEqual
import java.util.function.Predicate

sealed class TestList<out A>

object Nil: TestList<Nothing>()
data class Cons<out A>(
    val head: A,
    val tail: TestList<A>
): TestList<A>()

sealed class TestListUtil<out A> {
    companion object {
        fun <A> of(vararg aa: A) : TestList<A> {
            val tail = aa.sliceArray(1 until aa.size)
            return if (aa.isEmpty()) Nil else Cons(aa[0], of(*tail))
        }

        fun sum(ints: TestList<Int>): Int =
            when (ints) {
                is Nil -> 0
                is Cons -> ints.head + sum(ints.tail)
            }

        fun product(doubles: TestList<Double>): Double =
            when (doubles) {
                is Nil -> 1.0
                is Cons -> if (doubles.head == 0.0) 0.0
                else doubles.head * product(doubles.tail)
            }

        fun <A> append(a1: TestList<A>, a2: TestList<A>): TestList<A> =
            when(a1) {
                is Nil -> a2
                is Cons -> Cons(a1.head, append(a1.tail, a2))
            }

    }
}

class _03_함수형_데이터_구조: StringSpec ({

    "3.1 tail 함수를 만들어라" {
        fun <A> TestList<A>.tail() : TestList<A> =
            when(this) {
                is Cons -> this.tail
                is Nil -> Nil
            }
        TestListUtil.of("A", "B", "C", "D").tail() shouldBeEqual TestListUtil.of("B", "C", "D")
        TestListUtil.of(Nil).tail() shouldBeEqual Nil
    }

    "3.2 List 첫 원소를 다른 값으로 대치하는 setHead 함수를 만들어라" {
        fun <A> TestList<A>.setHead(x: A) : TestList<A> =
            when(this) {
                is Cons -> Cons(x, this.tail)
                is Nil -> Nil
            }
        TestListUtil.of(1, 2, 3, 4).setHead(5) shouldBeEqual TestListUtil.of(5, 2, 3, 4)
        TestListUtil.of(Nil).setHead(5) shouldBeEqual Nil
    }

    "3.3 List 맨 앞부터 n개 원소를 제거하는 drop 함수를 만들어라" {
        fun <A> TestList<A>.drop(n: Int) : TestList<A> =
            if (n <= 0) this
            else when(this) {
                is Cons -> this.tail.drop(n - 1)
                is Nil -> Nil
            }
        val list = TestListUtil.of(1, 2, 3, 4)
        list.drop(2) shouldBeEqual TestListUtil.of(3, 4)
        list.drop(4) shouldBeEqual Nil
        list.drop(5) shouldBeEqual Nil
    }

    "3.4 List 맨 앞에서부터 주어진 술어를 만족(술어 함수가 true를 반환)하는 연속적인 원소를 삭제하는 dropWhile 함수를 만들어라" {
        fun <A> TestList<A>.dropWhile(predicate: Predicate<A>) : TestList<A> =
            when(this) {
                is Cons ->
                    if(predicate.test(this.head)) this.tail.dropWhile(predicate)
                    else this
                is Nil -> Nil
            }

        val isEven : (Int) -> Boolean = { it % 2 == 0 }
        val list = TestListUtil.of(2,4,6,8,5,10,20)
        list.dropWhile(isEven) shouldBeEqual TestListUtil.of(5,10,20)

        val list1 = TestListUtil.of(2,4,6)
        list1.dropWhile(isEven) shouldBeEqual Nil
    }

    "3.5 List 에서 마지막 원소를 제외한 나머지 원소로 이뤄진 새 List 를 반환하는 init 함수를 만들어라" {
        fun <A> TestList<A>.init() : TestList<A> =
            when(this) {
                is Cons ->
                    if(this.tail == Nil) Nil
                    else Cons(this.head, this.tail.init())
                is Nil -> Nil
            }

        val list = TestListUtil.of("A", "B", "C", "D")
        list.init() shouldBeEqual TestListUtil.of("A", "B", "C")
    }
})
