package _20_Operator

import io.kotest.core.spec.style.StringSpec

class Component :StringSpec({

    "component" {
        val map = mapOf(
            1 to "a",
            2 to "b"
        )

        for ((int, str) in map) {
            println("$int : $str")
        }


    }
})
