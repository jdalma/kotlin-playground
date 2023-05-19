package _06_Generic

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe

class GenericTest : DescribeSpec({

    describe("Box 클래스") {
        context("정렬은") {
            val boxes = listOf(
                Box(1),
                Box(10),
                Box(3),
                Box(5),
                Box(7),
                Box(3),
            )

            it("size 프로퍼티 순으로 오름차순 정렬한다.") {
                val sorted = boxes.sorted()

                sorted.forEachIndexed { index, box ->
                    if (index > 0) {
                        sorted[index - 1].size shouldBeLessThanOrEqual sorted[index].size
                    }
                }
            }
        }
    }

    describe("Triple 클래스") {

        val triple1 = Triple(1,2,3)
        val triple2 = Triple("first",2,3.5)

        context("3개의 필드를 보유한다.") {
            triple1.first shouldBe 1
            triple1.second shouldBe 2
            triple1.third shouldBe 3

            triple2.first shouldBe "first"
            triple2.second shouldBe 2
            triple2.third shouldBe 3.5
        }

        context("reverse 함수는") {
            it("세 원소 순서를 뒤집은 새로운 Triple 객체를 반환한다.") {
                triple1.reverse() shouldBe Triple(3,2,1)
                triple2.reverse() shouldBe Triple(3.5,2,"first")
            }
        }

        context("toString 함수는") {
            it("'(첫 번째, 두 번째, 세 번째)'의 형식으로된 문자열을 반환한다.") {
                triple1.toString() shouldBe "(1, 2, 3)"
                triple2.toString() shouldBe "(first, 2, 3.5)"
            }
        }
    }

    describe("Account 클래스") {
        val account = Account(10000.5)
        val compare1 = Account(10001.5)
        val compare2 = Account(10000.0)
        context("compareTo 메서드는") {

            it("비교 대상 객체의 잔액이 더 크다면 음수를 반환한다.") {
                account.compareTo(compare1) shouldBeLessThan 0
            }

            it("비교 대상 객체의 잔액이 더 작다면 양수를 반환한다.") {
                account.compareTo(compare2) shouldBeGreaterThan 0
            }
        }
    }

    describe("copyWhenGenerator 함수는") {
        val param1 = listOf("a", "b", "c", "d") to "b"
        val param2 = listOf(
                StringBuilder("A"),
                StringBuilder("B"),
                StringBuilder("C")
        ) to StringBuilder("B")

        context("threshold 보다 큰 값만 반환한다.") {

            copyWhenGenerator(param1.first, param1.second) shouldBe listOf("c" , "d")
            copyWhenGenerator(param2.first, param2.second) shouldBe listOf("C")
        }
    }

    describe("OutBox<T: Number>") {
        it("제네릭 타입 Int와 Number의 관계는 성립되지 않는다.") {
            val intOutBox = OutBox<Int>(10)
            val numberOutBox = OutBox<Number>(10.0)

//            val unknownBox1: OutBox<Number> = intOutBox // 컴파일 에러
//            unknownBox1.get() shouldBe 10 // get()을 통해 값을 꺼내 업캐스팅이 되므로 여기서는 컴파일 에러가 나지 않는다.

            val unknownBox2: OutBox<Number> = numberOutBox
            unknownBox2.get() shouldBe 10.0

        }
    }

    describe("OutBox2<out T: Number>") {
        it("공변 (제네릭 타입의 상하위 타입 관계를 추론하게끔)") {
            val intOutBox = OutBox_공변<Int>(10)
            val numberOutBox = OutBox_공변<Number>(10.0)

            val unknownBox1: OutBox_공변<Number> = intOutBox
            unknownBox1.get() shouldBe 10

            var unknownBox2: OutBox_공변<Number> = numberOutBox
            unknownBox2.get() shouldBe 10.0
        }
    }

    describe("InBox<T: Number>") {
        it("제네릭 타입 Int와 Number의 관계는 성립되지 않는다.") {
            val intInBox = InBox<Int>(10)
            val numberInBox = InBox<Number>(10.0)

//            val unknownBox1: InBox<Number> = intInBox // 컴파일 에러
//            unknownBox1.set(10.0) // 업 캐스팅이 자동으로 되므로 컴파일 에러가 나지 않는다.

            val unknownBox2: InBox<Number> = numberInBox
            unknownBox2.set(10.0)

            val unknownBox3: InBox<Int> = intInBox
            unknownBox3.set(10)

//            val unknownBox4: InBox<Int> = numberInBox // 컴파일 에러
//            unknownBox3.set(10)
        }
    }

    describe("InBox_반공변<T: Number>") {
        it("Number 제네릭 타입을 Int 타입으로 변환할 수 있다.") {
            val intInBox = InBox_반공변<Int>(10)
            val numberInBox = InBox_반공변<Number>(10.0)

            val unknownBox: InBox_반공변<Int> = numberInBox
            unknownBox.toString() shouldBe "InBox_반공변(v=10.0)"
        }
    }
})
