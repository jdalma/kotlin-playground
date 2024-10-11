package leetcode_study

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class `minimum-window-substring` {

    /**
     * 다시 풀어야됨
     * TC: O(n), SC: O(1)
     */
    fun minWindow(s: String, t: String): String {
        val used = IntArray(128).apply {
            t.forEach { this[it.code]++ }
        }
        var count = t.length
        var (start, end) = 0 to 0
        var (minLen, startIndex) = Int.MAX_VALUE to 0

        while (end < s.length) {
            if (used[s[end++].code]-- > 0) {
                count--
            }
            while (count == 0) {
                if (end - start < minLen) {
                    startIndex = start
                    minLen = end - start
                }
                if (start < s.length && used[s[start++].code]++ == 0) {
                    count++
                }
            }
        }

        return if (minLen == Integer.MAX_VALUE) "" else s.substring(startIndex, startIndex + minLen)
    }

    @Test
    fun `입력받은 s 문자열에서 t의 모든 문자가 포함되는 최소 윈도우 부분 문자열을 반환한다`() {
        minWindow("ADOBECODEBANC", "ABC") shouldBe "BANC"
    }
}
