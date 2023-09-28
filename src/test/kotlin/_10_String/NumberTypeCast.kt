package _10_String

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.equals.shouldBeEqual

class NumberTypeCast : StringSpec ({

    "수 타입 간의 타입 변환" {
        Long.MAX_VALUE shouldBeEqual 9223372036854775807
        Long.MAX_VALUE.toDouble() shouldBeEqual 9.223372036854776E18
        Int.MAX_VALUE shouldBeEqual 2147483647
        Int.MAX_VALUE.toDouble() shouldBeEqual 2.147483647E9
        Int.MAX_VALUE.toFloat() shouldBeEqual 2.14748365E9f
    }
})
