package _04_ClassAndObject

import _03_Function.Function
import java.lang.UnsupportedOperationException
import kotlin.properties.Delegates

/**
 * 상속 관계
 */
open class Shape(
    val x: Double,
    val y: Double
) {
    fun paint() : String = "Shape($x,$y)"
}

class Circle(
    x : Double,
    y : Double,
    val radius : Double
) : Shape(x , y)


/**
 * 상속 관게에서 부모 필드 오버라이딩
 */
open class Parent(open val x: Int)

open class Child1(override val x: Int): Parent(x)
class Child2(x: Int): Parent(x)

class GrandChild1(override val x: Int): Child1(x)
// class GrandChild2(val x: Int): Child1(x) // x' hides member of supertype 'Child1' and needs 'override' modifier

/**
 * 추상 클래스
 * 추상 멤버가 있는 경우라면 클래스도 추상 클래스로 지정되어야 한다.
 */
abstract class ClassWithOverridable(
    val value1: String
) {
    // val value2: String 컴파일 에러

    abstract var value3: String
    var value4 = this.value3

    abstract fun mustOverride(param: Double): Int

    fun setter(value: String) {
        this.value3 = value
    }
}

/**
 * 인터페이스
 */

open class Person(val name: String) {
    override fun toString(): String = "Person($name)"
}

class PersonWithAge(name: String, val age:Int): Person(name), Comparable<PersonWithAge> {
    override fun toString(): String = "PersonWithAge($name,$age)"
    override fun compareTo(other: PersonWithAge): Int {
        if (other is PersonWithAge) {
            return this.age - other.age
        } else {
            throw UnsupportedOperationException("PersonWithAge와 다른 타입의 검사는 허용하지 않습니다.")
        }
    }
}

/**
 * 인터페이스 오버라이드 규칙
 */
interface IA {
    fun toString2(): String = "IA"
}
interface IB: IA {
    override fun toString2(): String = "IB: IA"
}
interface IC {
    fun toString2(): String = "IC"
}

open class A: IA {
    override fun toString(): String = "A: Any"
}
open class B: A(), IB {
    override fun toString(): String = "B: A: Any"
}

class C: B(), IB
class D: IA, B()
class E: IB, IC {
    override fun toString2(): String = "E: ${super<IB>.toString2()}: ${super<IC>.toString2()}"
}

/**
 * getter 와 setter 지정해주기
 */

class GetterSetterTest {
    var value1: Int = 0
        get(): Int = field
        set(param) {
            println("value1 setter called : $param")
            field = param
        }

    val value2: String
        get() = "value2"

    val value3
        get(): String = "value3"

    val value4 = "init data"
//        get() = "value4"  // 에러
//        get() = value4    // 에러
        get() = field

    val value5: String
        get(): String {
            return "value5"
        }

    val value6: String
        get() {
            return "value6"
        }

    init {
        value1 = 999
    }
}

/**
 * 지연 초기화 필드
 */
class LateInitTest {
    lateinit var value: String

    fun initValue() {
        value = "init"
    }
}
