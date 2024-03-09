package _22_Reflection

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.io.Serializable
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method


class ReflectionTest: DescribeSpec({

    data class Address(
        private val state: String,
        private val city: String
    )

    data class Person(
        private val name: String,
        var age: Int,
        private val address: Address
    ) {
        constructor(name: String, age: Int): this(name, age, Address("empty", "empty"))
        @Throws(IllegalArgumentException::class)
        constructor(age: Int): this("Admin", age, Address("Admin", "Admin"))
        fun greeting() = "안녕하세요."
    }

    describe("Person 클래스") {
        val personClass: Class<Person> = Person::class.java

        it("Constructor를 통해 Person 만들기") {
            val constructors: Array<Constructor<Person>>  = personClass.constructors as Array<Constructor<Person>>
            // {modifier} {constructor} {parameter types} {exception types}
            constructors[0].toString() shouldBe "public _22_Reflection.ReflectionTest\$1\$Person(java.lang.String,int)"
            constructors[1].toString() shouldBe "public _22_Reflection.ReflectionTest\$1\$Person(int) throws java.lang.IllegalArgumentException"
            constructors[2].toString() shouldBe "public _22_Reflection.ReflectionTest\$1\$Person(java.lang.String,int,_22_Reflection.ReflectionTest\$1\$Address)"

            val nameAndAgeConstructor = constructors[0]
            val ageConstructor = constructors[1]

            nameAndAgeConstructor.newInstance("Reflection", 10) shouldBe Person("Reflection", 10)
            ageConstructor.newInstance(10) shouldBe Person(10)
        }

        it("Field로 객체 내부 멤버변수 조회하기") {
            val fields: Array<Field> = personClass.declaredFields
            fields[0].toString() shouldBe "private final java.lang.String _22_Reflection.ReflectionTest$1\$Person.name"
            fields[1].toString() shouldBe "private int _22_Reflection.ReflectionTest$1\$Person.age"
            fields[2].toString() shouldBe "private final _22_Reflection.ReflectionTest$1\$Address _22_Reflection.ReflectionTest$1\$Person.address"

            val person = Person(100)

            // age 필드는 var로 선언되어 있어서 필드 접근이 가능할 것으로 생각하지만 코틀린에서는 가변 필드를 getter/setter 로만 접근하기 때문에 필드 자체는 private이다.
            shouldThrowExactly<IllegalAccessException> { fields[0].get(person) }
                .message shouldBe "class _22_Reflection.ReflectionTest\$1\$1\$2 cannot access a member of class _22_Reflection.ReflectionTest\$1\$Person with modifiers \"private final\""
            shouldThrowExactly<IllegalAccessException> { fields[1].get(person) }
            shouldThrowExactly<IllegalAccessException> { fields[2].get(person) }

            fields.forEach { it.trySetAccessible() }

            val nameField = fields[0]
            val ageField = fields[1]
            val addressField = fields[2]

            nameField.get(person) shouldBe "Admin"
            ageField.get(person) shouldBe 100
            addressField.get(person) shouldBe Address("Admin", "Admin")
        }

        it("Method 정보로 객체 실행하기") {
            val methods: Array<Method> = personClass.methods

            val getAgeMethod = methods.find { it.name == "getAge" }!!
            val setAgeMethod = methods.find { it.name == "setAge" }!!
            val greetingMethod = methods.find { it.name == "greeting" }!!

            val person = Person(100)

            person.age shouldBe 100
            getAgeMethod.invoke(person) shouldBe 100

            setAgeMethod.invoke(person, 50)

            person.age shouldBe 50
            getAgeMethod.invoke(person) shouldBe 50

            greetingMethod.invoke(person) shouldBe "안녕하세요."
        }
    }

    @MyAnnotation
    open class Parent

    @MyAnnotation(name = "child", number = 100)
    class Child(val member: String): Parent(), Comparable<Child>, Serializable {
        override fun compareTo(other: Child): Int = 1
    }

    describe("상속 관계의 클래스") {
        val childClass: Class<Child> = Child::class.java

        it("자기 자신에게 작성된 어노테이션 조회") {
            val myAnnotation = childClass.getAnnotation(MyAnnotation::class.java)

            myAnnotation.name shouldBe "child"
            myAnnotation.number shouldBe 100
        }

        it("부모 클래스 조회 및 부모 클래스에 작성된 어노테이션 조회") {
            val superClass: Class<in Child> = childClass.superclass
            superClass.name shouldBe "_22_Reflection.ReflectionTest$1\$Parent"

            val myAnnotation = superClass.getAnnotation(MyAnnotation::class.java)
            myAnnotation.name shouldBe "default"
            myAnnotation.number shouldBe 10000
        }

        it("구현 인터페이스 조회") {
            val implement = childClass.interfaces
            implement[0].name shouldBe "java.lang.Comparable"
            implement[1].name shouldBe "java.io.Serializable"
        }
    }
})

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class MyAnnotation(
    val name: String = "default",
    val number: Int = 10000
)
