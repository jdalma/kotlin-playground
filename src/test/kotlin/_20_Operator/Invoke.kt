package _20_Operator

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.equals.shouldBeEqual

class Invoke: StringSpec ({

    class Adder {
        operator fun invoke(vararg v: Int) = v.sum()
    }

    "호출 연산자" {
        val adder = Adder()
        adder() shouldBeEqual 0
        adder(1) shouldBeEqual 1
        adder(1,2,3,4,5) shouldBeEqual 15
        adder(1,2,3,4,5,6,7,8,9,10) shouldBeEqual 55

        val array = intArrayOf(1,2,3,4,5)
        adder(*array) shouldBeEqual 15

        val array2 = intArrayOf(1,2,*array,3,4,5)
        adder(*array2) shouldBeEqual 30
    }
})
