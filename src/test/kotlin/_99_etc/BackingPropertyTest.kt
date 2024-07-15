package _99_etc

import io.kotest.matchers.equals.shouldBeEqual
import org.junit.jupiter.api.Test


class LottoNumbers1(val numbers: List<Int>)

open class LottoNumbers2(numbers: List<Int>) {
    private val _numbers: List<Int> = numbers.toList()
    val numbers: List<Int> get() = _numbers
    var score: Int = 0
        set(value) {
            field = if(value >= 0) value else 0
        }

}

class BackingPropertyTest {

    @Test
    fun case() {
        val numbers = mutableListOf(1, 2, 3, 4, 5)
        val lottoNumber1 = LottoNumbers1(numbers)
        numbers.add(6)

        lottoNumber1.numbers.size shouldBeEqual 6

        val lottoNumbers2 = LottoNumbers2(numbers)
        numbers.add(7)

        lottoNumbers2.numbers.size shouldBeEqual 6
    }
}
