package _07_NullTest

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.lang.IllegalArgumentException
import kotlin.random.Random

class NullTest: DescribeSpec ({

    describe("타입 파라미터가 T인 find 함수는") {
        val includeNullArr = arrayOf(null , 20 , 300 , 100 , 4)
        val notIncludeNullArr = arrayOf(20 , 300 , 100 , 4)
        fun <T> find(arr: Array<T>, predicate: (T) -> Boolean) : T {
            return arr.first(predicate)
        }
        it("배열의 원소가 null이어도 허용한다.") {
            find(includeNullArr) { it != null && it >= 100 }?.shouldBeEqual(300)
            find(notIncludeNullArr) { it >= 100 } shouldBeEqual 300
        }
    }

    describe("T 타입을 Any로 제한한 find 함수는") {
        val notIncludeNullArr = arrayOf(20 , 300 , 100 , 4)
        fun <T: Any> find(arr: Array<T>, predicate: (T) -> Boolean) : T {
            return arr.first(predicate)
        }
        it("배열의 원소가 null이라면 허용하지 못한다.") {
//          find(includeNullArr) { it != null && it >= 100 } // 컴파일 에러
            find(notIncludeNullArr) { it >= 100 } shouldBeEqual 300
        }
    }

    describe("T 타입을 Any?로 제한한 find 함수는") {
        val includeNullArr = arrayOf(null , 20 , 300 , 100 , 4)
        val notIncludeNullArr = arrayOf(20 , 300 , 100 , 4)
        fun <T: Any?> find(arr: Array<T>, predicate: (T) -> Boolean) : T {
            return arr.first(predicate)
        }
        it("배열의 원소가 null이어도 허용한다.") {
            find(includeNullArr) { it != null && it >= 100 }?.shouldBeEqual(300)
            find(notIncludeNullArr) { it >= 100 } shouldBeEqual 300
        }
    }

    describe("T 타입 update 함수는") {
        val includeNullArr = arrayOf(null , 20 , 300 , 100 , 4)
        val notIncludeNullArr = arrayOf(20 , 300 , 100 , 4)
        fun <T> update(array: Array<T>, transform: (T) -> T) : Array<T> {
            for (i in array.indices) {
                array[i] = transform(array[i])
            }
            return array
        }

        it("배열의 원소가 null이어도 허용한다.") {
            update(includeNullArr) { if(it == null) 1 else it + 1 } shouldBe arrayOf(1, 21 , 301 , 101 , 5)
            update(notIncludeNullArr) { it + 1 } shouldBe arrayOf(21 , 301 , 101 , 5)
        }
    }

    describe("T를 Any로 제한한 update 함수는") {
        val notIncludeNullArr = arrayOf(20 , 300 , 100 , 4)
        fun <T : Any> update(array: Array<T>, transform: (T) -> T) : Array<T> {
            for (i in array.indices) {
                array[i] = transform(array[i])
            }
            return array
        }

        it("배열의 원소가 null이면 허용하지 않는다.") {
//            update(includeNullArr) { if(it == null) 1 else it + 1 } // 컴파일 에러
            update(notIncludeNullArr) { it + 1 } shouldBe arrayOf(21 , 301 , 101 , 5)
        }
    }

    describe("Nothing과 Unit 반환 타입에 따른 널 추론") {
        fun getIntOrNull() : Int? = if(Random.nextBoolean()) Random.nextInt(0, 1000) else null

        it("항상 예외가 발생하고 Nothing을 반환하는 함수") {
            fun alwaysFail(i: Int?) : Nothing { throw Throwable("항상 예외 발생") }

            val number = getIntOrNull()
            if (number == null || number > 900) {
                runCatching {
                    alwaysFail(number)
                }
            }
            val twice = number?.times(2) // twice를 Int 타입으로 추론
        }

        it("항상 예외가 발생하고 Unit을 반환하는 함수") {
            fun alwaysFail(i: Int?) : Unit { throw Throwable("항상 예외 발생") }

            val number = getIntOrNull()
            if (number == null || number > 900) {
                runCatching {
                    alwaysFail(number)
                }
            }
            val twice = number?.times(2) // number의 널을 허용한다.
        }
    }

    describe("널이 될 수 있는 타입의 값과 is,as 연산") {
        val v1: Int? = null
        val v2: Int? = 10

        (v1 is Int?) shouldBeEqual true
        (v1 is Int) shouldBeEqual false
        (v2 is Int?) shouldBeEqual true
        (v2 is Int) shouldBeEqual true
    }

    describe("엘비스 연산자") {
        val string = ""

        // firstOrNull이 Char? 타입을 반환하기 때문에 코드 연쇄를 쓸 수 없다.
//        val firstLetterDoubleString = string.firstOrNull().code.toDouble().toString() // 컴파일 에러

        val firstLetterDoubleString0 = string.firstOrNull()?.code?.toDouble()
        val firstLetterDoubleString1 = string.firstOrNull()?.code?.toDouble().toString()
        val firstLetterDoubleString2 = string.firstOrNull()?.code?.toDouble()?.toString()
        val firstLetterDoubleString3 = string.firstOrNull()?.code?.toDouble()?.toString() ?: ""
        val firstLetterDoubleString4 = (string.firstOrNull()?.code?.toDouble() ?: 0.0).toString()

        shouldThrow<IllegalArgumentException> {
            string.firstOrNull()?.code?.toDouble()?.toString() ?: throw IllegalArgumentException("널이에요")
        }

        firstLetterDoubleString0 shouldBe null
        firstLetterDoubleString1 shouldBeEqual "null"
        firstLetterDoubleString2 shouldBe null
        firstLetterDoubleString3 shouldBeEqual ""
        firstLetterDoubleString4 shouldBeEqual "0.0"
    }

    describe("클래스 계층 관계에서 널의 가상 타입이 포함된다면?") {
        open class Parent {}
        class Child: Parent() {}

        it("is의 관계") {
            val parent = Parent()
            val child = Child()

            (child is Child) shouldBeEqual true
            (child is Child?) shouldBeEqual true
            (child is Parent) shouldBeEqual true
            (child is Parent?) shouldBeEqual true
            (parent is Child) shouldBeEqual false
            (parent is Child?) shouldBeEqual false
            (parent is Parent) shouldBeEqual true
            (parent is Parent?) shouldBeEqual true

            val parentNullable: Parent? = Parent()
            val childNullable: Child? = Child()

            (childNullable is Child) shouldBeEqual true
            (childNullable is Child?) shouldBeEqual true
            (childNullable is Parent) shouldBeEqual true
            (childNullable is Parent?) shouldBeEqual true
            (parentNullable is Child) shouldBeEqual false
            (parentNullable is Child?) shouldBeEqual false
            (parentNullable is Parent) shouldBeEqual true
            (parentNullable is Parent?) shouldBeEqual true
        }
    }
})
