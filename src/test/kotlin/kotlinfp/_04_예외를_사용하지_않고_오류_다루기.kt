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

    /**
     * Option이 Some인 경우 결과를 반환하지만 Option이 None인 경우 주어진 기본 값을 반환한다.
     */
    fun <ITEM: Any> Option<ITEM>.getOrElse(default: () -> ITEM): ITEM =
        when(this) {
            is None -> default()
            is Some -> this.item
        }

    /**
     * 첫 번째 Option의 값이 정의된 경우(즉, Some인 경우) 그 Option을 반환한다.
     * 그렇지 않은 경우 두 번째 Option을 반환한다.
     */
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

class _04_예외를_사용하지_않고_오류_다루기: StringSpec ({


    "mean" {
        fun mean(list: List<Double>) : Option<Double> =
            if(list.isEmpty()) None
            else Some(list.sum() / list.size)

        val option = mean(listOf(1.1, 5.5))

    }

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
})
