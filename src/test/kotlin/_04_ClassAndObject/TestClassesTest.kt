package _04_ClassAndObject

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe

class ClassTest : ShouldSpec ({

    context("StudentScore") {
        val studentScore = StudentScore(
            "테스트",
            90,
            80,
            100
        )

        should("sum() 함수는 과목의 점수를 합산하여 반환한다.") {
            studentScore.sum() shouldBeEqual 270
        }
    }

    context("Foo") {
        val foo = Foo("제목")

        should(".allTitle() 함수는 [제목!_제목]을 리턴한다.") {
            foo.allTitle() shouldBeEqual "제목!_제목"
        }
    }

    context("초기화 블록") {
        val init = InitExample("abc")

        should("result 필드는 생성자 파라미터와 그 길이를 반환한다.") {
            init.result shouldBeEqual "abc:3"
        }
    }

    context("상속 관계 클래스") {
        val circle = Circle(1.5, 3.0, 1.2)

        should("자식 객체에서 부모 메소드를 호출할 수 있다.") {
            circle.paint() shouldBeEqual "Shape(1.5,3.0)"
        }

        val child1 = Child1(10)
        val child2 = Child2(10)
        should("자식이 부모 멤버 필드를 오버라이딩할 수 있다.") {
            child1.x shouldBeEqual 10
            child2.x shouldBeEqual 10
        }
    }

    context("is 와 as, 스마트 캐스트") {
        should("is 연산자는 어떤 타입에 속하는지 알아볼 수 있다.") {
            val number: Number = 10
            val string = "abc"

            (number is Int) shouldBeEqual true
            (number is Number) shouldBeEqual true
            (number is Double) shouldBeEqual false
            (string is String) shouldBeEqual true
        }

        should("as 연산자는 타입을 변환 시킬 수 있다.") {
            val child = Child1(10)

            ((child as Parent) is Parent) shouldBeEqual true
        }
    }

    context("인터페이스 오버라이드 규칙") {
        val a = A()
        val b = B()
        val c = C()
        val d = D()
        val e = E()

        should("toString 과 toString2 함수의 결과는") {
            a.toString() shouldBeEqual "A: Any"
            b.toString() shouldBeEqual "B: A: Any"
            c.toString() shouldBeEqual "B: A: Any"
            d.toString() shouldBeEqual "B: A: Any"

            a.toString2() shouldBeEqual "IA"
            b.toString2() shouldBeEqual "IB: IA"
            c.toString2() shouldBeEqual "IB: IA"
            d.toString2() shouldBeEqual "IB: IA" // 나는 "IA"가 출력될 줄 알았다..
            e.toString2() shouldBeEqual "E: IB: IA: IC"
        }
    }

    context("클래스 필드 Setter를 직접 정의했을 때") {

        should("init block에서 필드를 재정의하면 직접 정의한 Setter가 호출된다.") {
            val setterTest = GetterSetterTest()
        }
    }

    context("지연 초기화 필드") {
        val obj = LateInitTest()

        should("초기화 하지 않고 사용하면 예외가 발생한다.") {
            shouldThrow<UninitializedPropertyAccessException> {
                obj.value shouldBe null
            }
        }

        should("초기화 하고 사용하면 예외가 발생하지 않는다.") {
            obj.initValue()
            obj.value shouldBeEqual "init"
        }
    }

    context("null is T로 변수의 널 허용성을 확인할 수 있다.") {
        val test = LateInitTest()
        test.nullCheck() shouldBe booleanArrayOf(false,true)
    }

    context("class의 val 필드가 함수를 호출하면 생성자 호출 시점에 초기화 된다.") {

        class Count {
            var count = 10
                get() : Int {
                    return field--
                }
        }
        class Test {
            private val countObj = Count()
            private val data = countObj.count
            fun getData() = this.data
        }

        val test = Test()
        test.getData() shouldBe 10
        test.getData() shouldBe 10
        test.getData() shouldBe 10
        test.getData() shouldBe 10
    }
})
