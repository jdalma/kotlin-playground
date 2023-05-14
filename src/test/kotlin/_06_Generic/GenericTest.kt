package _06_Generic

import io.kotest.core.spec.style.DescribeSpec
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

})
