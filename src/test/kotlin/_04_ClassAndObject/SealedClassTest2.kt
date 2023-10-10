package _04_ClassAndObject

import io.kotest.core.spec.style.StringSpec
import io.kotest.core.spec.style.Test
import io.kotest.matchers.equals.shouldBeEqual

interface Expression {
    fun eval(env: Map<String, Double>): Double = when(this) {
        is VariableExpr -> env.getValue(this.name)
        is NumberExpr -> this.value
        is BinOpExpr -> this.op(this.e1.eval(env), this.e2.eval(env))
        else -> throw IllegalStateException("Unknown type ${this::class.qualifiedName}")
    }
}

enum class BinOp {
    PLUS {
        override operator fun invoke(v1: Double, v2: Double) = v1 + v2
    },
    MINUS {
        override operator fun invoke(v1: Double, v2: Double) = v1 - v2
    },
    TIMES {
        override operator fun invoke(v1: Double, v2: Double) = v1 * v2
    },
    DIVIDES{
        override operator fun invoke(v1: Double, v2: Double) = v1 / v2
    };
    abstract operator fun invoke(v1: Double, v2: Double): Double
}

data class VariableExpr(val name: String): Expression
data class NumberExpr(val value: Double): Expression
data class BinOpExpr(val op: BinOp, val e1: Expression, val e2: Expression) : Expression

class SealedClassTest2:StringSpec ({

    "Expression을 구현하는 식" {
        val twoTimesTwoPlusSix = BinOpExpr(
            BinOp.PLUS,
            BinOpExpr(BinOp.TIMES, NumberExpr(2.0), NumberExpr(2.0)), NumberExpr(6.0)
        )
        "(2*2)+6 = ${twoTimesTwoPlusSix.eval(emptyMap())}" shouldBeEqual "(2*2)+6 = 10.0"

        val aSquared = BinOpExpr(BinOp.TIMES, VariableExpr("a"), VariableExpr("a"))
        "aSquared = ${aSquared.eval(mapOf("a" to 3.0))}" shouldBeEqual "aSquared = 9.0"
    }
})


fun interface MyFunction2<T1, T2, R> {
    operator fun invoke(v1: T1, v2: T2): R
}

sealed class Op: MyFunction2<Int, Int, Int>

object PLUS: Op() {
    override fun invoke(v1: Int, v2: Int): Int = v1 + v2
}

object MINUS: Op() {
    override fun invoke(v1: Int, v2: Int): Int = v1 - v2
}
