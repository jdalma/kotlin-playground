package _09_Collection

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class CollectionUtils :StringSpec ({

    "filter, filterNot 사용해보기" {
        val array = arrayOf(1,2,3,4,5)
        val list = listOf(1,2,3,4,5)
        val set = setOf(1,2,3,4,5)
        val map = mapOf(
            "one" to 1,
            "two" to 2,
            "three" to 3
        )

        array.filter { it % 2 == 0 } shouldBe arrayOf(2,4)
        list.filter { it % 2 == 0 } shouldBe listOf(2,4)
        set.filter { it % 2 == 0 } shouldBe setOf(2,4)
        map.filter { it.key.startsWith("t") } shouldBe mapOf("two" to 2,"three" to 3)

        array.filterNot { it % 2 == 0 } shouldBe arrayOf(1,3,5)
        list.filterNot { it % 2 == 0 } shouldBe listOf(1,3,5)
        set.filterNot { it % 2 == 0 } shouldBe setOf(1,3,5)
        map.filterNot { it.key.startsWith("t") } shouldBe mapOf("one" to 1)
    }

    "filterNotNull 사용해보기" {
        val generateOrNull : (Int) -> Int? = { if (it % 2 == 0) null else it * 2 }
        val list = arrayOf(1,2,3,4,5).map(generateOrNull)

        list shouldBe arrayOf(2,null,6,null,10)
        list.filterNotNull() shouldBe arrayOf(2,6,10)
    }


    "indexOf, lastIndexOf, indexOfFirst, indexOfLast 사용해보기" {
        val list = listOf("A", "B", "C", "D", "E", "A")

        list.indexOf("A") shouldBe 0
        list.indexOf("not-exist") shouldBe -1

        list.lastIndexOf("A") shouldBe 5
        list.lastIndexOf("E") shouldBe 4
        list.lastIndexOf("not-exist") shouldBe -1

        list.indexOfFirst { it == "A" } shouldBe 0
        list.indexOfLast { it == "A" } shouldBe 5
    }

    "map, mapNotNull 사용해보기" {
        val array = arrayOf(1, 2, 3, 4, 5)
        val set = setOf(1, 2, 3, 4, 5)

        array.map { it * 2 } shouldBe arrayOf(2, 4, 6, 8, 10)
        set.map { it * 2 } shouldBe arrayOf(2, 4, 6, 8, 10)

        val list = listOf(1,null,2,null,3)
        list.mapNotNull { it?.times(2) } shouldBe listOf(2,4,6)

        data class Person(
            var name: String,
            var age: Int
        )

        val map = mapOf(
            "one" to Person("one", 10),
            "two" to Person("two", 20),
            "three" to Person("three", 30)
        )

        map.map { it.value.age * 2 } shouldBe listOf(20,40,60)
    }

    "flatten 사용해보기" {
        val sentence = "This is an example of a sentence".split(" ")
        val chars = sentence.map { it.toList() }

        chars shouldBe listOf(
            listOf('T', 'h', 'i', 's'),
            listOf('i', 's'),
            listOf('a', 'n'),
            listOf('e', 'x', 'a', 'm', 'p', 'l', 'e'),
            listOf('o', 'f'),
            listOf('a'),
            listOf('s', 'e', 'n', 't', 'e', 'n', 'c', 'e')
        )

        // Array<Array<원소타입>> 이나 Iterable<Iterable<원소타입>> 인 경우에만 정의되며, 모든 원소를 펼쳐서 연결한 단일 배열이나 리스트를 돌려준다.

        chars.flatten() shouldBe listOf(
            'T', 'h', 'i', 's',
            'i', 's',
            'a', 'n',
            'e', 'x', 'a', 'm', 'p', 'l', 'e',
            'o', 'f',
            'a',
            's', 'e', 'n', 't', 'e', 'n', 'c', 'e'
        )
    }

    /**
     * map()에 전달하는 변환 함수의 반환 타입이 리스트 등의 컬렉션 타입인 경우가 자주 있고,
     * 그럴 때마다 flatten()을 호출하는 것은 귀찮기 때문에 코틀린은 map()과 flatten()을 합친 flatMap()을 제공한다.
     */
    "flatMap 사용해보기" {
        val sentence = "This is an example of a sentence"
                                    .split(" ")
                                    .flatMap { it.toList() }

        sentence shouldBe listOf(
            'T', 'h', 'i', 's',
            'i', 's',
            'a', 'n',
            'e', 'x', 'a', 'm', 'p', 'l', 'e',
            'o', 'f',
            'a',
            's', 'e', 'n', 't', 'e', 'n', 'c', 'e'
        )
    }

    /**
     * map()에 대해서도 원소 인덱스와 원소를 함께 람다에 전달해주는 mapIndexed()가 있다.
     */
    "mapIndexed 와 flatMapIndexed" {
        val words = "This is an example of a sentence".split(" ")

        val charList1 = words
            .map { it.toList() }
            .flatMap {
                it.mapIndexed { index, c ->
                    if (index == 0) c.uppercaseChar()
                    else c
                }
            }

        charList1 shouldBe listOf(
            'T', 'h', 'i', 's', 'I', 's', 'A', 'n', 'E', 'x', 'a', 'm', 'p', 'l', 'e', 'O', 'f', 'A', 'S', 'e', 'n', 't', 'e', 'n', 'c', 'e'
        )

        val charList2 = words.flatMapIndexed { index, wordList ->
            if (index % 2 == 0) wordList.map {it.uppercaseChar()}
            else wordList.toList() }

        charList2 shouldBe listOf(
            'T', 'H', 'I', 'S', 'i', 's', 'A', 'N', 'e', 'x', 'a', 'm', 'p', 'l', 'e', 'O', 'F', 'a', 'S', 'E', 'N', 'T', 'E', 'N', 'C', 'E'
        )
    }
})
