package _06_Generic

import java.lang.IllegalArgumentException

class Box(
    val size: Int
) : Comparable<Box>{

    override fun compareTo(other: Box): Int {
        return this.size - other.size
    }

}

class Triple<F, S, T>(
    val first: F,
    val second: S,
    val third: T
) {

    fun reverse() : Triple<T, S, F> {
        return Triple(third, second, first)
    }

    override fun toString(): String {
        return "($first, $second, $third)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Triple<*, *, *>

        if (first != other.first) return false
        if (second != other.second) return false
        return third == other.third
    }

    override fun hashCode(): Int {
        var result = first?.hashCode() ?: 0
        result = 31 * result + (second?.hashCode() ?: 0)
        result = 31 * result + (third?.hashCode() ?: 0)
        return result
    }
}
