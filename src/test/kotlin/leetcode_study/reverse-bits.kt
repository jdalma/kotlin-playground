package leetcode_study

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class `reverse-bits` {

    /**
     * 결과 정수에 입력 받은 정수의 "and 1" 연산을 통해 한 비트씩 누적하며, 입력 받은 정수는 오른쪽으로 쉬프트하면서 한 비트씩 버린다.
     * 시간복잡도: O(1), 공간복잡도: O(1)
     */
    fun reverseBits(n:Int):Int {
        var number = n
        var result = 0

        repeat(32) {
            result = result shl 1
            result += number and 1
            number = number ushr 1
        }

        return result
    }

    @Test
    fun name() {
        reverseBits(0B00000010100101000001111010011100) shouldBe 964176192
        reverseBits(0B11111111111111111111111111111101.toInt()) shouldBe -1073741825
    }
}
