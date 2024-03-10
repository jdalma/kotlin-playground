import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import kotlin.collections.Collection


data class LottoNumber(val number: Int)

class LottoNumbers: HashSet<LottoNumber>() {
    var addCount = 0
        private set

    override fun add(element: LottoNumber): Boolean {
        addCount++
        return super.add(element)
    }

    override fun addAll(elements: Collection<LottoNumber>): Boolean {
        addCount += elements.size
        return super.addAll(elements)
    }
}

/**
 * 상위 클래스의 메서드 동작을 다시 구현하는 이 방식은 어렵고 시간도 더 들고 자칫 오류를 내거나 성능을 떨어뜨릴 수도 있다.
 * 기존 클래스를 확장하는 대신 새로운 클래스를 만들고 private 필드로 기존 클래스의 인스턴스를 참조하게 하자
 * 상속은 반드시 하위 클래스가 상위 클래스의 "진짜" 하위 타입인 상황에서만 쓰여야 한다.
 * 클래스 B가 클래스 A와 is-a 관계일 때만 사용해야 한다. "B가 정말 A인가?"
 *
 * 클래스의 행동을 확장하는 것이 아니라 정제할 때다.
 * 확장이란 새로운 행동을 덧붙여 기존의 행동을 부분적으로 보완하는 것을 의미하고, 정제란 부분적으로 불완전한 행동을 완전하게 만드는 것을 의미한다.
 */
class LSP {

    @Test
    fun add() {
        val numbers = LottoNumbers()
        numbers.add(LottoNumber(1))
        numbers.add(LottoNumber(2))
        numbers.add(LottoNumber(3))
        numbers.add(LottoNumber(4))
        numbers.add(LottoNumber(5))

        numbers.addCount shouldBe 5
    }

    /**
     * 이 부분이 LSP를 위반한 것이다.
     * HashSet의 addAll을 호출하는 것 자체가 내부의 오버라이드된 add를 호출하는 것이기 때문에 addCount가 2배로 나오는 것
     */
    @Test
    fun addAll() {
        val numbers = LottoNumbers()
        numbers.addAll(
            listOf(
                LottoNumber(1),
                LottoNumber(2),
                LottoNumber(3),
                LottoNumber(4)
            )
        )

        numbers.addCount shouldBe 8
    }
}
