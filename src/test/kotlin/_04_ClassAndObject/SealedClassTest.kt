package _04_ClassAndObject

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.equals.shouldBeEqual
import java.lang.IllegalArgumentException

interface Expr
class Num(val value: Int): Expr
class Sum(val left: Expr, val right: Expr): Expr

sealed interface SealedExpr {
    class Num(val value: Int): SealedExpr
    class Sum(val left: SealedExpr, val right: SealedExpr): SealedExpr
}

sealed class SealedClassExpr {
    class Num(val value: Int): SealedClassExpr()
    class Sum(val left: SealedClassExpr, val right: SealedClassExpr): SealedClassExpr()
}


class SealedClassTest: StringSpec ({

    "else 분기가 꼭 필요하다." {
        fun eval(e: Expr): Int =
            when (e) {
                is Num -> e.value
                is Sum -> eval(e.left) + eval(e.right)
                else -> throw IllegalArgumentException("Unknown expression")
            }

        eval(Sum(Num(5), Num(10))) shouldBeEqual 15
    }

    "sealed interface 사용하면 디폴트 분기가 필요없다." {
        fun eval(e: SealedExpr): Int =
            when(e) {
                is SealedExpr.Num -> e.value
                is SealedExpr.Sum -> eval(e.left) + eval(e.right)
            }
        eval(SealedExpr.Sum(SealedExpr.Num(5), SealedExpr.Num(10))) shouldBeEqual 15
    }

    "sealed class 를 사용하면 디폴트 분기가 필요없다." {
        fun eval(e: SealedClassExpr): Int =
            when(e) {
                is SealedClassExpr.Num -> e.value
                is SealedClassExpr.Sum -> eval(e.left) + eval(e.right)
            }
        eval(SealedClassExpr.Sum(SealedClassExpr.Num(5), SealedClassExpr.Num(10))) shouldBeEqual 15
    }
})
