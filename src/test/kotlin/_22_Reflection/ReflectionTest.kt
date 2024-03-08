package _22_Reflection

import io.kotest.core.spec.style.StringSpec

class ReflectionTest: StringSpec({

    data class Address(
        private val state: String,
        private val city: String
    )

    open class Person(
        private val name: String,
        private var age: Int,
        private val address: Address
    ) {
        constructor(name: String, age: Int): this(name, age, Address("empty", "empty"))
        constructor(age: Int): this("Admin", age, Address("Admin", "Admin"))

        open fun greeting() = "안녕하세요."
    }

    class Marathoner(
        name: String,
        age: Int,
        private val awards: List<String> = emptyList()
    ): Person(name, age) {
        override fun greeting() = "안녕하세요~"
    }

    "Person 클래스" {
        val personClass: Class<*> = Class.forName("")

        "생성자 실행하기" {

        }
    }
})
