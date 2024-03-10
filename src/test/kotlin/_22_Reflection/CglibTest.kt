package _22_Reflection

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import net.sf.cglib.proxy.*
import java.lang.reflect.Method


class CglibTest: StringSpec ({

    open class Person: Hello, Goodbye {

        open fun greeting() = "안녕하세요!"

        override fun sayHello(name: String): String = "Hello $name"

        override fun sayHi(name: String): String = "Hi $name"

        override fun sayThankYou(name: String): String = "Thank you $name"

        override fun sayGoodbye(name: String): String = "Goodbye $name"
    }

    class MyMethodInterceptor: MethodInterceptor {
        override fun intercept(proxy: Any, method: Method, args: Array<out Any>, methodProxy: MethodProxy): Any =
            // 프록시의 슈퍼클래스의 타겟 메서드를 호출한다.
            "(Intercepted) " + methodProxy.invokeSuper(proxy, args)
    }

    class MyFixedValue: FixedValue {
        override fun loadObject(): Any = "Intercepted and always return \"Fixed\""
    }

    class MyCallbackFilter : CallbackFilter {
        override fun accept(method: Method): Int =
            when(method.name) {
                "greeting" -> 1 // FixedValue 콜백 사용
                else -> 0       // MethodInterceptor 사용
            }

    }

    "Person 클래스 Cglib 테스트" {
        val proxy = Enhancer.create(
            Person::class.java,
            MyMethodInterceptor()
        )

        (proxy is Hello) shouldBe true
        (proxy is Goodbye) shouldBe true
        (proxy is Person) shouldBe true

        val person = proxy as Person
        person.greeting() shouldBe "(Intercepted) 안녕하세요!"
        person.sayHello("admin") shouldBe "(Intercepted) Hello admin"
        person.sayHi("admin") shouldBe "(Intercepted) Hi admin"
        person.sayThankYou("admin") shouldBe "(Intercepted) Thank you admin"
        person.sayGoodbye("admin") shouldBe "(Intercepted) Goodbye admin"
    }

    "Person 클래스 Cglib Callback, FixedValue 테스트" {
        val proxy = Enhancer.create(
            Person::class.java,
            arrayOf(Hello::class.java, Goodbye::class.java),
            MyCallbackFilter(),
            arrayOf<Callback>(MyMethodInterceptor(), MyFixedValue())
        )

        val person = proxy as Person
        person.greeting() shouldBe "Intercepted and always return \"Fixed\""
        person.sayHello("admin") shouldBe "(Intercepted) Hello admin"
        person.sayHi("admin") shouldBe "(Intercepted) Hi admin"
        person.sayThankYou("admin") shouldBe "(Intercepted) Thank you admin"
        person.sayGoodbye("admin") shouldBe "(Intercepted) Goodbye admin"
    }
})
