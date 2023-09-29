package _20_Operator

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.equals.shouldBeEqual

class Infix: StringSpec ({

    class Inventory {
        val stock = mutableListOf<String>()

        operator fun plusAssign(s: String) {
            stock.add(s)
        }
        operator fun minusAssign(s: String) {
            stock.minusAssign(s)
        }
        infix operator fun contains(s: String) = stock.contains(s)
    }

    class Foo(val v: Int) {
        infix operator fun plus(other: Foo) = this.v + other.v
    }

    "중위 표기법 함수" {
        val inventory = Inventory()
        inventory += "딸기"
        inventory += "바나나 우유"
        inventory contains "딸기" shouldBeEqual true

        inventory -= "딸기"
        inventory contains "딸기" shouldBeEqual false

        Foo(1) + Foo(2) shouldBeEqual 3
        Foo(1).plus(Foo(2)) shouldBeEqual 3
        Foo(1) plus Foo(2) shouldBeEqual 3
    }
})
