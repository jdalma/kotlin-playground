package _20_Operator

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.equals.shouldBeEqual

class Plus: StringSpec ({

    class Rational(
        val numerator: Int,
        val denominator: Int
    ) {
        private val step = 2

        operator fun plus(other: Rational): Rational =
            Rational(
                this.numerator * other.denominator + this.denominator * other.numerator,
                this.denominator * other.denominator
            )
        operator fun plus(other: Int): Rational =
            Rational(
                this.numerator + other,
                this.denominator + other
            )
        operator fun unaryPlus(): Rational =
            Rational(
                this.numerator + step,
                this.denominator + step
            )
        operator fun unaryMinus(): Rational =
            Rational(
                this.numerator - step,
                this.denominator - step
            )

        override fun toString(): String {
            return "$numerator/$denominator"
        }
    }

    "연산자" {
        val rat1 = Rational(2,3)
        val rat2 = Rational(3,5)

        (rat1 + rat2).toString() shouldBeEqual "19/15"
        (rat1 + 10).toString() shouldBeEqual "12/13"

        (+rat1).toString() shouldBeEqual "4/5"
        (-rat1).toString() shouldBeEqual "0/1"
    }


})
