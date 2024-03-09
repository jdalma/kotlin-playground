package _22_Reflection

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

interface Hello {
    fun sayHello(name: String): String
    fun sayHi(name: String): String
    fun sayThankYou(name: String): String
}

class DynamicProxyTest: DescribeSpec({

    class HelloTarget: Hello {
        override fun sayHello(name: String): String = "Hello $name"
        override fun sayHi(name: String): String = "Hi $name"
        override fun sayThankYou(name: String): String = "Thank You $name"
    }

    class HelloUppercase(
        private val hello: Hello
    ): Hello {
        override fun sayHello(name: String): String = hello.sayHello(name).uppercase()
        override fun sayHi(name: String): String = hello.sayHi(name).uppercase()
        override fun sayThankYou(name: String): String = hello.sayThankYou(name).uppercase()
    }

    class UppercaseHandler(
        private val target: Hello
    ): InvocationHandler {
        override fun invoke(proxy: Any, method: Method, args: Array<out Any>): Any {
            val result: String = method.invoke(target, args) as String
            return result.uppercase()
        }
    }

    describe("Hello 동적 프록시 테스트하기") {
        val hello = Proxy.newProxyInstance(
            Hello::class.java.classLoader,
            arrayOf(Hello::class.java),
            UppercaseHandler(HelloTarget())
        ) as Hello

        hello.sayHello("admin") shouldBe "HELLO ADMIN"
    }
})
