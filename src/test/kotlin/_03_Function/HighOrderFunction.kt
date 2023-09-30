package _03_Function

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.equals.shouldBeEqual


object Example {

    fun abs(n: Int) = if (n < 0) -n else n

    fun factorial(i: Int) : Int {
        fun go(n: Int, acc: Int): Int =
            if (n <= 0) acc
            else go(n - 1, acc * n)

        return go(i, 1)
    }

    fun formatAbs(x: Int): String {
        val msg = "The absolute value of %d is %d"
        return msg.format(x, abs(x))
    }

    fun formatFactorial(x: Int): String {
        val msg = "The factorial of %d is %d"
        return msg.format(x , factorial(x))
    }

    fun formatResult(name: String, n: Int, f: (Int) -> Int): String {
        val msg = "The %s of %d is %d"
        return msg.format(name, n, f(n))
    }
}

class HighOrderFunction: StringSpec ({

    "절댓값" {
        val number = -13
        Example.formatAbs(number) shouldBeEqual "The absolute value of -13 is 13"
    }

    "팩토리얼" {
        Example.formatFactorial(5) shouldBeEqual "The factorial of 5 is 120"
    }

    "함수 넘기기" {
        Example.formatResult("absolute value", -13, Example::abs) shouldBeEqual "The absolute value of -13 is 13"
        Example.formatResult("factorial", 5, Example::factorial) shouldBeEqual "The factorial of 5 is 120"
    }

    "다형적 함수 : 타입에 대해 추상화하기" {
        fun findFirst(ss: Array<String>, key: String): Int {
            tailrec fun loop(n: Int) : Int =
                when {
                    n >= ss.size -> -1
                    ss[n] == key -> n
                    else -> loop(n + 1)
                }

            return loop(0)
        }

        fun <T> findFirst(xs: Array<T>, p: (T) -> Boolean) : Int {
            tailrec fun loop(n: Int) : Int =
                when {
                    n >= xs.size -> -1
                    p(xs[n]) -> n
                    else -> loop(n + 1)
                }

            return loop(0)
        }

        val strings = arrayOf("A", "B", "C", "C")
        val numbers = arrayOf(1,2,3,3)
        val chars = arrayOf('a', 'b', 'c', 'c')

        findFirst(strings, "C") shouldBeEqual 2

        findFirst(strings) { e -> e == "C" } shouldBeEqual 2
        findFirst(numbers) { e -> e == 3 } shouldBeEqual 2
        findFirst(chars) { e -> e == 'c' } shouldBeEqual 2
    }

    "타입에 맞춰 구현하기" {
        fun <A, B, C> partial1(a: A, f: (A, B) -> C): (B) -> C = TODO()

        fun <A, B, C> partial2(a: A, f: (A, B) -> C): (B) -> C =
            { b: B -> TODO() }

        fun <A, B, C> partial3(a: A, f: (A, B) -> C): (B) -> C =
            { b: B -> f(a, b) }
    }
})
