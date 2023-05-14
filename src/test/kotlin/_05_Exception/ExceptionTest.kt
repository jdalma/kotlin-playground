package _05_Exception

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.lang.Exception

class ExceptionTest: DescribeSpec ({

    describe("상속 관계인 예외 클래스") {

        val parent = CommonException("부모 예외 클래스")
        val child1 = FirstLevelException1("1레벨 자식 예외 클래스")
        val child2 = SecondLevelException("2레벨 자식 예외 클래스")
        val other = OtherException("그 외 예외 클래스")

        context("자식 클래스 is 부모 클래스는 참이다.") {
            (child1 is CommonException) shouldBe true
            (child2 is FirstLevelException1) shouldBe true
            (child2 is CommonException) shouldBe true
//            (other is CommonException) shouldBe false // 컴파일 에러
        }

        context("throwException 함수는") {
            it("난수를 기준으로 예외를 던진다.") {
                try {
                    throwException()
                } catch (e: Throwable) {
                    when (e) {
                        is SecondLevelException -> println("2레벨 자식 예외 클래스")
                        is FirstLevelException1 -> println("1레벨 자식 예외 클래스")
                        is CommonException -> println("부모 예외 클래스")
                    }
                } catch (_: OtherException) {
                    println("그 외 예외 클래스")
                }
            }
        }
    }

    describe("StringToInt 클래스는") {
        val str = StringToInt("문자열")
        val number = StringToInt("1234")

        context("한글을 정수로 변환을 시도한다면") {

            it("throwException 함수는 NumberFormatException 을 던진다.") {
                shouldThrow<NumberFormatException> { str.throwException()  }
            }

            it("returnZero 함수는 0을 반환한다.") {
                str.returnZero() shouldBe 0
            }
        }

        context("숫자 문자열을 정수로 변환을 시도한다면") {
            it("정수를 반환한다.") {
                number.throwException() shouldBe 1234
                number.returnZero() shouldBe 1234
            }
        }
    }

    describe("finally 블록에서 return문을 선언한다면") {
        val exception = FinallyBlock()
        it("다른 return문은 무시된다.") {
            exception.func1() shouldBe "finally"
            exception.func2() shouldBe "finally"
            exception.func3() shouldBe "finally"
        }
    }

})
