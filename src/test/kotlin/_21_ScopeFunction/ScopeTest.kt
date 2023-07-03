package _21_ScopeFunction

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ScopeTest : StringSpec ({

    "this 키워드를 사용하는 스코프 함수는 쉐도잉 문제가 발생할 수 있다." {
        class Item (
            var id: String,
            var price: Int
        )
        val price = 10000
        val item = Item("test" , 20000)

        item.run {
            id shouldBe "test"
            price shouldBe 10000
        }

        item.run {
            this.id shouldBe "test"
            this.price shouldBe 20000
        }

        item.let {
            it.id shouldBe "test"
            it.price shouldBe 20000
        }
    }

    "with 스코프 함수는 dot notation이 필요없다" {
        class Person {
            var name: String = ""
            var age: Int = 0
        }

        val person = Person()
        with(person) {
            name = "test"
            age = 10
        }

        person.name shouldBe "test"
        person.age shouldBe 10

    }
})
