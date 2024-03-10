package _22_Reflection

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

interface Hello {
    fun sayHello(name: String): String
    fun sayHi(name: String): String
    fun sayThankYou(name: String): String
}

interface Goodbye {
    fun sayGoodbye(name: String): String
    fun sayThankYou(name: String): String
}
class DynamicProxyTest: StringSpec({

    class HelloTarget: Hello {
        override fun sayHello(name: String): String = "Hello $name"
        override fun sayHi(name: String): String = "Hi $name"
        override fun sayThankYou(name: String): String = "Hello Thank You $name"
    }

    class GoodbyeTarget: Goodbye {
        override fun sayGoodbye(name: String): String = "Goodbye $name"
        override fun sayThankYou(name: String): String = "Goodbye Thank You $name"
    }

    class UppercaseHandler(
        helloTarget: Hello,
        goodbyeTarget: Goodbye
    ): InvocationHandler {

        private val lookupTarget = mapOf(
            Hello::class.java to helloTarget,
            Goodbye::class.java to goodbyeTarget,
        )
        override fun invoke(proxy: Any, method: Method, args: Array<out Any>): Any {
            val result: String = method.invoke(lookupTarget[method.declaringClass], *args) as String
            return result.uppercase()
        }
    }

    "Hello 동적 프록시 테스트하기" {
        val proxy = Proxy.newProxyInstance(
            javaClass.classLoader,
            arrayOf(Hello::class.java, Goodbye::class.java),
            UppercaseHandler(HelloTarget(), GoodbyeTarget())
        )

        (proxy is HelloTarget) shouldBe false
        (proxy is GoodbyeTarget) shouldBe false
        (proxy is Hello) shouldBe true
        (proxy is Goodbye) shouldBe true

        val hello = proxy as Hello
        hello.sayHello("admin") shouldBe "HELLO ADMIN"
        hello.sayHi("admin") shouldBe "HI ADMIN"
        hello.sayThankYou("admin") shouldBe "HELLO THANK YOU ADMIN"

        val goodbye = proxy as Goodbye
        goodbye.sayGoodbye("admin") shouldBe "GOODBYE ADMIN"
        goodbye.sayThankYou("admin") shouldBe "HELLO THANK YOU ADMIN"
    }
})
