package kotlinfp

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.equals.shouldBeEqual
import kotlinfp.Option.*
import kotlinfp.Option.None.filter
import kotlinfp.Option.None.flatMap
import kotlinfp.Option.None.flatMap_2
import kotlinfp.Option.None.getOrElse
import kotlinfp.Option.None.map
import kotlinfp.Option.None.orElse
import kotlin.math.pow

sealed class Option<out ITEM: Any> {
    data class Some<out ITEM: Any>(val item: ITEM) : Option<ITEM>()
    object None: Option<Nothing>()

    companion object {
        fun <ITEM: Any> empty(): Option<ITEM> = None
        fun <ITEM: Any> of(item: ITEM): Option<ITEM> = Some(item)
    }

    fun <ITEM: Any, RESULT: Any> Option<ITEM>.map(block: (ITEM) -> RESULT): Option<RESULT> =
        when(this) {
            is None -> this
            is Some -> Some(block(this.item))
        }

    fun <ITEM: Any, RESULT: Any> Option<ITEM>.flatMap(block: (ITEM) -> Option<RESULT>): Option<RESULT> =
        this.map(block).getOrElse { None }

    fun <ITEM: Any> Option<ITEM>.getOrElse(default: () -> ITEM): ITEM =
        when(this) {
            is None -> default()
            is Some -> this.item
        }

    fun <ITEM: Any> Option<ITEM>.orElse(default: () -> Option<ITEM>): Option<ITEM> =
        this.map { Some(it) }.getOrElse { default() }

    fun <ITEM: Any> Option<ITEM>.filter(predicate: (ITEM) -> Boolean) : Option<ITEM> =
        this.flatMap { item -> if(predicate(item)) Some(item) else None }

    fun <ITEM: Any, RESULT: Any> Option<ITEM>.flatMap_2(block: (ITEM) -> Option<RESULT>): Option<RESULT> =
        when(this) {
            is None -> this
            is Some -> block(this.item)
        }
    fun <ITEM: Any> Option<ITEM>.orElse_2(default: () -> Option<ITEM>): Option<ITEM> =
        when(this) {
            is None -> default()
            is Some -> this
        }
    fun <ITEM: Any> Option<ITEM>.filter_2(predicate: (ITEM) -> Boolean) : Option<ITEM> =
        when(this) {
            is None -> this
            is Some -> if(predicate(this.item)) this else None
        }
}

fun <ITEM: Any, RESULT: Any> lift(block: (ITEM) -> RESULT) : (Option<ITEM>) -> Option<RESULT> =
    { oa -> oa.map(block) }
fun <ITEM: Any> catches(block: () -> ITEM) : Option<ITEM> =
    try { Some(block()) } catch(e: Throwable) { None }

class _04_예외를_사용하지_않고_오류_다루기: StringSpec ({

    "Option.map" {
        val none = Option.empty<String>()
        val some = Option.of("test")
        val toUppercase : (String) -> String = { it.uppercase() }

        some.map(toUppercase) shouldBeEqual Option.of("TEST")
        none.map(toUppercase) shouldBeEqual None
    }

    "Option.flatMap" {
        val none = Option.empty<Int>()
        val some = Option.of(123456)
        val toString : (Int) -> Option<String> = { Option.of(it.toString()) }

        some.flatMap(toString) shouldBeEqual Option.of("123456")
        some.flatMap_2(toString) shouldBeEqual Option.of("123456")

        none.flatMap(toString) shouldBeEqual None
    }

    "Option.getOrElse" {
        val none = Option.empty<String>()
        val some = Option.of("ABCDE")
        val default : () -> String = { "INIT" }

        some.getOrElse(default) shouldBeEqual "ABCDE"
        none.getOrElse(default) shouldBeEqual "INIT"
    }

    "Option.orElse" {
        val none = Option.empty<String>()
        val some = Option.of("TEST")
        val default : () -> Option<String> = { Option.of("INIT") }

        some.orElse(default) shouldBeEqual Option.of("TEST")
        none.orElse(default) shouldBeEqual Option.of("INIT")
    }

    "Option.filter" {
        val none = Option.empty<Int>()
        val some = Option.of(21)
        val isOdd : (Int) -> Boolean = { it % 2 != 0 }
        val isEven : (Int) -> Boolean = { it % 2 == 0 }

        some.filter(isOdd) shouldBeEqual Option.of(21)
        some.filter(isEven) shouldBeEqual None

        none.filter(isOdd) shouldBeEqual None
    }

    "4.2 flatMap을 사용해 variance 함수를 구현하라" {
        // 시퀀스의 평균이 m이면, variance는 시퀀스의 원소를 x라 할 때, x - m을 제곱한 값의 평균이다.
        // (x - m).pow(2)라 할 수 있다.
        fun mean(list: List<Double>) : Option<Double> =
            if(list.isEmpty()) None
            else Some(list.sum() / list.size)

        fun variance(list: List<Double>) : Option<Double> =
            mean(list).flatMap { m ->
                mean(list.map { x -> (x - m).pow(2) })
            }

        val list = listOf(1.1, 2.2, 3.3)
        variance(list) shouldBeEqual Option.of(0.8066666666666665)
        variance(emptyList()) shouldBeEqual Option.empty()

    }

    "4.3 두 Option 값을 이항 함수를 통해 조합하는 제네릭 함수 map2 를 작성하라." {
        // 두 Option 중에 하나라도 None이 존재하면 반환값도 None이다.
        fun <ITEM1: Any, ITEM2: Any, RESULT: Any> map2(
            item1: Option<ITEM1>,
            item2: Option<ITEM2>,
            block: (ITEM1, ITEM2) -> RESULT
        ): Option<RESULT> = TODO()

    }

})
