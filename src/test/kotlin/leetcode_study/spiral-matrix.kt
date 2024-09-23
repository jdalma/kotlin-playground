package leetcode_study

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class `spiral-matrix` {

    fun spiralOrder(matrix: Array<IntArray>): List<Int> {
        val result = mutableListOf<Int>()

        TODO()
    }

    @Test
    fun `정수 2차원 배열을 나선형 1차원 정수로 반환한다`() {
        spiralOrder(arrayOf(
            intArrayOf(1,2,3),
            intArrayOf(4,5,6),
            intArrayOf(7,8,9)
        )) shouldBe listOf(1,2,3,6,9,8,7,4,5)
        spiralOrder(arrayOf(
            intArrayOf(1,2,3,4),
            intArrayOf(5,6,7,8),
            intArrayOf(9,10,11,12)
        )) shouldBe listOf(1,2,3,4,8,12,11,10,9,5,6,7)

    }
}
