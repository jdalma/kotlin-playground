import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainOnly
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldStartWith
import kotlin.random.Random

@DisplayName("기본 테스트")
class BasicTest : FunSpec({

    test("더하기") {
        1 + 10 shouldBeEqual 11
    }

    test("곱하기") {
        1.0 * 2.0 shouldBeEqual 2.0
    }

    test("문자열을 정수로 변환할 수 없을 떄") {
        val string = "123a456"
        val exception = shouldThrow<NumberFormatException> {
            string.toInt()
        }
        exception.message.shouldStartWith("For input string: \"$string\"")
    }

    test("부호가 있는 정수와 없는 정수") {
        val unsigned = (-1).toUInt()
        val signed = unsigned.toInt()

        unsigned shouldBeEqual 4294967295.toUInt()
        signed shouldBeEqual -1
    }

    test("반복문") {
        val count = 10
        for (i in 1..count) {
            i shouldBeLessThanOrEqual count
        }

        var randomNumber1 = Random.nextInt(0, 100)
        while (randomNumber1 < 50) {
            println("while 난수 = $randomNumber1")
            randomNumber1 shouldBeLessThan 50
            randomNumber1 = Random.nextInt(0, 100)
        }
        println("while $randomNumber1 발생, 루프 종료")

        randomNumber1 = 0
        do {
            println("do while 난수 = $randomNumber1")
            randomNumber1 shouldBeLessThan 50
            randomNumber1 = Random.nextInt(0, 100)
        } while (randomNumber1 < 50)

        println("do while $randomNumber1 발생, 루프 종료")
    }

    test("단항 부호 연산자") {
        var number = 10;
        -number shouldBeLessThan 0
        +number shouldBeGreaterThan 0
    }

    test("배열을 생성하는 세 가지 방법") {
        val oneToTen = arrayOf(1,2,3,4,5)
        oneToTen.size shouldBeEqual 5
        shouldThrow<ArrayIndexOutOfBoundsException> {
            oneToTen[5]
        }

        val arrayOf10Ints = arrayOfNulls<Int>(10)
        arrayOf10Ints.shouldContainOnly(null)

        val arrayOf1To10 = Array(10) {it + 1}
        arrayOf1To10.forEachIndexed { index, value ->
            value shouldBeEqual index + 1
            value shouldBeLessThan 11
            value shouldNotBe 0
        }
    }

    test("배열 기본 연산") {
        val arrayOf1To10 = Array(10) {it + 1}
        val intRange = arrayOf1To10.indices

        arrayOf1To10.size shouldBeEqual 10
        arrayOf1To10.first() shouldBeEqual 1
        arrayOf1To10.last() shouldBeEqual 10

        intRange.first shouldBeEqual 0
        intRange.start shouldBeEqual 0
        intRange.last shouldBeEqual 9
        intRange.endInclusive shouldBeEqual 9
        intRange.step shouldBeEqual 1

        (1 in arrayOf1To10) shouldBeEqual true
        (11 in arrayOf1To10) shouldBeEqual false
    }
})
