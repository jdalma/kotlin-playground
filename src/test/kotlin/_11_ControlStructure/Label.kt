package _11_ControlStructure

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.equals.shouldBeEqual

class Label: StringSpec ({

    "break" {
        var innerBreakCount = 0
        var outerBreakCount = 0
        outer@ for (i in 2..9) {
            inner@ for (j in 2..9) {
                if ( i * j > 60) {
                    innerBreakCount++
                    break@inner
                }
            }
        }
        innerBreakCount shouldBeEqual 3

        outer@ for (i in 2..9) {
            inner@ for (j in 2..9) {
                if ( i * j > 60) {
                    outerBreakCount++
                    break@outer
                }
            }
        }
        outerBreakCount shouldBeEqual 1
    }
})
