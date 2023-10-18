package kotlinfp

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.equals.shouldBeEqual
import kotlinfp.Either.*
import kotlinfp.Option.*
import kotlinfp.Option.None.filter
import kotlinfp.Option.None.flatMap
import kotlinfp.Option.None.flatMap_2
import kotlinfp.Option.None.getOrElse
import kotlinfp.Option.None.map
import kotlinfp.Option.None.orElse
import kotlinfp.TestList.*
import org.assertj.core.condition.Not
import org.assertj.core.internal.bytebuddy.implementation.bytecode.Throw
import kotlin.math.pow

sealed class Option<out VALUE: Any> {
    data class Some<out VALUE: Any>(val item: VALUE) : Option<VALUE>()
    object None: Option<Nothing>()

    object Unwrap{
        inline operator fun <VALUE: Any> Option<VALUE>.component1(): VALUE =
            when(this) {
                is None -> throw Throwable()
                is Some -> this.item
            }
    }

    companion object {
        inline operator fun <VALUE: Any> invoke(block: Unwrap.() -> VALUE) : Option<VALUE> =
            try { Option(Unwrap.block()) } catch (e: Throwable) { None }
        fun <VALUE: Any> empty(): Option<VALUE> = None
        fun <VALUE: Any> of(item: VALUE): Option<VALUE> = Some(item)

        operator fun <VALUE: Any> invoke(): Option<VALUE> = None
        operator fun <VALUE: Any> invoke(item: VALUE): Option<VALUE> = Some(item)

        inline fun <VALUE: Any> catches(throwBlock: () -> VALUE): Option<VALUE> =
            try { Option(throwBlock()) } catch (e: Throwable) { None }
    }

    fun <VALUE: Any, OTHER: Any> Option<VALUE>.map(block: (VALUE) -> OTHER): Option<OTHER> =
        when(this) {
            is None -> this
            is Some -> Option(block(this.item))
        }

    fun <VALUE: Any, OTHER: Any> Option<VALUE>.flatMap(block: (VALUE) -> Option<OTHER>): Option<OTHER> =
        this.map(block).getOrElse { empty() }

    /**
     * Option으로 반환되지 않고 값 그 자체로 반환되기 때문에 termination(종결점) 이라고 불린다.
     * 대부분 getXXX을 termintation으로 사용한다.
     */
    fun <VALUE: Any> Option<VALUE>.getOrElse(default: () -> VALUE): VALUE =
        when(this) {
            is None -> default()
            is Some -> this.item
        }

    fun <VALUE: Any> Option<VALUE>.orElse(default: () -> Option<VALUE>): Option<VALUE> =
        this.map { Option(it) }.getOrElse(default)

    fun <VALUE: Any> Option<VALUE>.filter(predicate: (VALUE) -> Boolean) : Option<VALUE> =
        this.flatMap { item -> if(predicate(item)) Option(item) else empty() }

    fun <VALUE: Any, RESULT: Any> Option<VALUE>.flatMap_2(block: (VALUE) -> Option<RESULT>): Option<RESULT> =
        when(this) {
            is None -> this
            is Some -> block(this.item)
        }
    fun <VALUE: Any> Option<VALUE>.orElse_2(default: () -> Option<VALUE>): Option<VALUE> =
        when(this) {
            is None -> default()
            is Some -> this
        }
    fun <VALUE: Any> Option<VALUE>.filter_2(predicate: (VALUE) -> Boolean) : Option<VALUE> =
        when(this) {
            is None -> this
            is Some -> if(predicate(this.item)) this else None
        }
}

inline fun <VALUE: Any, OTHER: Any> List<VALUE>.transformOrDefault(
    default: () -> OTHER,
    block: (List<VALUE>) -> OTHER
) : OTHER = if (isNotEmpty()) block(this) else default()

fun <VALUE: Any, RESULT: Any> lift(block: (VALUE) -> RESULT) : (Option<VALUE>) -> Option<RESULT> =
    { oa -> oa.map(block) }
fun <VALUE: Any> catches(block: () -> VALUE) : Option<VALUE> =
    try { Some(block()) } catch(e: Throwable) { None }

sealed class Either<out E, out S> {
    data class Left<out E> @PublishedApi internal constructor(val value: E) : Either<E, Nothing>()
    data class Right<out S> @PublishedApi internal constructor(val value: S) : Either<Nothing, S>()

    companion object {
        inline fun <E: Any, S: Any> right(value: S): Either<E, S> = Right(value)
        inline fun <E: Any, S: Any> left(value: E): Either<E, S> = Left(value)
        inline fun <S: Any> r(value: S): Either<Nothing, S> = Right(value)
        inline fun <E: Any> l(value: E): Either<E, Nothing> = Left(value)

        inline operator fun <S: Any> invoke(block: Unwrap.() -> S): Either<Throwable, S> =
            try { Right(Unwrap.block()) } catch (e: Throwable) { Left(e) }
    }

    object Unwrap {
        inline operator fun <E: Any, S: Any> Either<E,S>.component1() : S =
            when(this) {
                is Left -> throw Throwable("$value")
                is Right -> value
            }
    }
}

fun <ERROR: Any, VALUE: Any, RESULT: Any> Either<ERROR, VALUE>.map(
    block: (VALUE) -> RESULT
) : Either<ERROR, RESULT> =
    when(this) {
        is Left -> this
        is Right -> Right(block(this.value))
    }

fun <ERROR: Any, VALUE: Any, RESULT: Any> Either<ERROR, VALUE>.flatMap(
    block: (VALUE) -> Either<ERROR, RESULT>
) : Either<ERROR, RESULT> =
    when(this) {
        is Left -> this
        is Right -> block(this.value)
    }

fun <ERROR: Any, VALUE: Any> Either<ERROR, VALUE>.getOrElse(
    default: () -> VALUE
) : VALUE = when(this) {
    is Left -> default()
    is Right -> this.value
}
fun <ERROR: Any, VALUE: Any> Either<ERROR, VALUE>.orElse(
    block: () -> Either<ERROR, VALUE>
): Either<ERROR, VALUE> =
    when(this) {
        is Left -> block()
        is Right -> this
    }

fun <ERROR: Any, VALUE1: Any, VALUE2: Any, RESULT: Any> map2(
    item1: Either<ERROR, VALUE1>,
    item2: Either<ERROR, VALUE2>,
    block: (VALUE1, VALUE2) -> RESULT
): Either<ERROR, RESULT> =
    item1.flatMap { a -> item2.map { b -> block(a, b) } }

fun <VALUE: Any> eitherCatches(block: () -> VALUE) : Either<Exception, VALUE>  =
    try { Right(block()) } catch(e: Exception) { Left(e) }

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
        // item2가 None일 수 있기 때문에 item1에서 flatMap을 펼치는 것
        fun <VALUE1: Any, VALUE2: Any, RESULT: Any> map2(
            item1: Option<VALUE1>,
            item2: Option<VALUE2>,
            block: (VALUE1, VALUE2) -> RESULT
        ): Option<RESULT> =
//            item1.flatMap { first ->
//                item2.map { second ->
//                    block(first, second)
//                }
//            }
            if (item1 is Some && item2 is Some) Option(block(item1.item, item2.item)) else Option.empty()


        val item1 = Option.of("AB")
        val item2 = Option.of("CD")
        val none = Option.empty<String>()
        val concat : (String, String) -> String = { p1, p2 -> p1 + p2 }

        map2(item1, item2, concat) shouldBeEqual Option.of("ABCD")
        map2(item1, none, concat) shouldBeEqual Option.empty()
    }

    "4.4 원소가 Option인 리스트를 리스트인 Option으로 합쳐주는 sequence 함수를 작성하라" {
        // 리스트안의 원소 중 None이 하나라도 존재한다면 결괏값은 None이어야 한다.
        fun <VALUE1: Any, VALUE2: Any, RESULT: Any> map2(
            item1: Option<VALUE1>,
            item2: Option<VALUE2>,
            block: (VALUE1, VALUE2) -> RESULT
        ): Option<RESULT> = item1.flatMap { first ->
            item2.map { second ->
                block(first, second)
            }
        }

        fun <VALUE: Any> sequence(list: TestList<Option<VALUE>>) : Option<TestList<VALUE>> =
            TestList.foldRight(
                list,
                Some(Nil)
            ) { it: Option<VALUE>, acc: Option<TestList<VALUE>> -> map2(it, acc, ::Cons) }

        val list = TestList(
                Option.of("A"),
                Option.of("B"),
                Option.of("C"),
                Option.of("D"),
                Option.of("E")
        )
        sequence(list) shouldBeEqual Option.of(
            TestList("A","B","C","D","E")
        )

        val none = TestList(
            Option.of("A"),
            Option.of("B"),
            Option.empty(),
            Option.of("D"),
            Option.of("E")
        )
        sequence(none) shouldBeEqual Option.empty()
    }

    "4.5 리스트를 단 한 번만 순회하는 traverse 함수를 구현하라" {
        fun <VALUE1: Any, VALUE2: Any, RESULT: Any> map2(
            item1: Option<VALUE1>,
            item2: Option<VALUE2>,
            block: (VALUE1, VALUE2) -> RESULT
        ): Option<RESULT> = item1.flatMap { first ->
            item2.map { second ->
                block(first, second)
            }
        }

        fun <VALUE: Any, RESULT: Any> traverse(
            list: TestList<VALUE>,
            block: (VALUE) -> Option<RESULT>
        ) : Option<TestList<RESULT>> =
            when(list) {
                is Nil -> Some(Nil)
                is Cons -> map2(
                    block(list.head),
                    traverse(list.tail, block)
                ) { item: RESULT, items: TestList<RESULT> -> Cons(item, items) }
            }

        val list = TestList(1,2,3,4,5)
        traverse(list) { e ->
            catches { e.toString() }
        } shouldBeEqual Option.of(TestList("1","2","3","4","5"))

        val list2 = TestList("1","2","3","$","5")
        traverse(list2) { s ->
            catches { s.toInt() }
        } shouldBeEqual None
    }

    "4.6 Right 값에 대해 활용할 수 있는 map, flatMap, orElse, map2를 구현하라" {
        val right: Either<Throwable, Int> = Right(100)
        val left: Either<Throwable, Int> = Left(Throwable("Exception!!!"))

        right.map { it.toString() } shouldBeEqual Right("100")
        left.map { it.toString() } shouldBeEqual left

        right.flatMap { item -> Right(item.toString()) } shouldBeEqual Right("100")
        left.flatMap { item -> Right(item.toString()) } shouldBeEqual left

        right.orElse { Left("Exception!!!") } shouldBeEqual right
        left.orElse { Left("Exception!!!") } shouldBeEqual  Left("Exception!!!")

        val right1: Right<Int> = Right(100)
        val right2: Right<Int> = Right(200)
        val left1: Either<Throwable, Int> =
            Left(IllegalArgumentException("IllegalArgumentException!!!"))
        val left2: Either<Throwable, Int> =
            Left(IllegalStateException("IllegalStateException!!!"))
        val plus : (Int, Int) -> Int = { a,b -> a + b }

        map2(right1, right2, plus)  shouldBeEqual Right(300)
        map2(right, left1, plus) shouldBeEqual left1
        map2(left1, left2, plus) shouldBeEqual left1
        map2(left2, left1, plus) shouldBeEqual left2
    }

    "4.7 Either에 대한 sequence와 traverse를 구현하라" {
        fun <ERROR: Any, VALUE: Any, RESULT: Any> traverse(
            list: TestList<VALUE>,
            block: (VALUE) -> Either<ERROR, RESULT>
        ): Either<ERROR, TestList<RESULT>> =
            when(list) {
                is Nil -> Right(Nil)
                is Cons ->
                    map2(block(list.head), traverse(list.tail, block)) { item, items ->
                        Cons(item, items)
                    }
            }

        fun <ERROR: Any, VALUE: Any> sequence(
            list: TestList<Either<ERROR, VALUE>>
        ) : Either<ERROR, TestList<VALUE>> =
            traverse(list) { it }

        fun <VALUE: Any> catches(block: () -> VALUE) : Either<String, VALUE>  =
            try { Right(block()) } catch(e: Throwable) { Left(e.message!!) }

        val list = TestList("1", "2", "3", "4", "5")
        traverse(list) { a ->
            catches { a.toInt() }
        } shouldBeEqual Right(TestList(1,2,3,4,5))

        val list2 = TestList("1", "2", "3", "4", "x")
        traverse(list2) { a ->
            catches { a.toInt() }
        } shouldBeEqual Left("""For input string: "x"""")

        val list3 = TestList(
            Right(1),
            Right(2),
            Right(3)
        )
        sequence(list3) shouldBeEqual Right(TestList(1, 2, 3))

        val list4 = TestList(
            Right(1),
            Right(2),
            Left("Exception!!!"),
            Right(3)
        )
        sequence(list4) shouldBeEqual Left("Exception!!!")
    }

    "4.8 두 개의 예외를 다 반환하려면 어떻게 해야할까?" {
        val right1: Right<Int> = Right(100)
        val right2: Right<Int> = Right(200)
        val left1: Either<Throwable, Int> =
            Left(IllegalArgumentException("IllegalArgumentException!!!"))
        val left2: Either<Throwable, Int> =
            Left(IllegalStateException("IllegalStateException!!!"))
        val plus : (Int, Int) -> Int = { a,b -> a + b }

        map2(left1, left2, plus) shouldBeEqual left1
        map2(left2, left1, plus) shouldBeEqual left2

        fun <L: Any, R1: Any, R2: Any, OTHER: Any> Either<L, R1>.map2LeftToList(
            other: Either<L, R2>,
            block: (R1, R2) -> OTHER
        ): Either<TestList<L>, OTHER> =
            when(this) {
                is Left -> when(other) {
                    is Left -> Left(TestList(value, other.value))
                    is Right -> Left(TestList(value))
                }
                is Right -> when(other) {
                    is Left -> Left(TestList(other.value))
                    is Right -> Right(block(value, other.value))
                }
            }

        right1.map2LeftToList(right2, plus) shouldBeEqual Either.right(300)
//        left1.map2LeftToList(left2, plus) shouldBeEqual Either.left(TestList(IllegalArgumentException("IllegalArgumentException!!!"), IllegalStateException("IllegalStateException!!!")))

        fun <L: Any, R1: Any, R2: Any, OTHER: Any> Either<L, R1>.map2LeftAddList(
            other: Either<TestList<L>, R2>,
            block: (R1, R2) -> OTHER
        ): Either<TestList<L>, OTHER> =
            when(this) {
                is Left -> when(other) {
                    is Left -> Left(Cons(value, other.value))
                    is Right -> Left(TestList(value))
                }
                is Right -> when(other) {
                    is Left -> other
                    is Right -> Right(block(value, other.value))
                }
            }
    }

    "Option.fx 함수에 대해" {
        val result = Option {
            val (a) = Option(3)
            val (b) = Option(5)
            a + b
        }.getOrElse { 0 }

        result shouldBeEqual 8
    }

    "Either.fx 함수에 대해" {
        val result = Either {
            val (a) = Either.r(5)
            val (b) = Either.r(5)
            a + b
        }.getOrElse { 0 }

        result shouldBeEqual 10

        val result2 = Either {
            val (a) = Either.r(5)
            val (b) = Either.l(5)
        }.getOrElse { 0 }

        result2 shouldBeEqual 0
    }
})
