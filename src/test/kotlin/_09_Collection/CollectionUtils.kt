package _09_Collection

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe

class CollectionUtils :StringSpec ({

    "제자리 정렬" {
        val array = intArrayOf(5,3,2,1,4)
        array.sort()
        array shouldBe intArrayOf(1,2,3,4,5)

        array.sortDescending()
        array shouldBe intArrayOf(5,4,3,2,1)

        val list = mutableListOf("a","e","d","c")
        list.sort()
        list shouldBe listOf("a","c","d","e")

        val students = arrayOf(
            Score("a", 50, 60),
            Score("b", 30, 10),
            Score("c", 70, 50),
            Score("d", 100, 80)
        )
        /**
         * sortBy()는 람다가 만들어낸 기준값을 바탕으로 정렬을 수행한다.
         */
        students.sortBy { it.kor + it.eng / 2 }
        students shouldBe arrayOf(
            Score("b", 30, 10),
            Score("a", 50, 60),
            Score("c", 70, 50),
            Score("d", 100, 80)
        )

        students.sortByDescending { it.kor + it.eng / 2 }
        students shouldBe arrayOf(
            Score("d", 100, 80),
            Score("c", 70, 50),
            Score("a", 50, 60),
            Score("b", 30, 10)
        )
    }

    "정렬된 복사본 리스트를 돌려주는 정렬" {
        val numbers = arrayOf(5,3,2,1,4)
        numbers.sorted() shouldBeEqual listOf(1,2,3,4,5)             // List로 반환된다.
        numbers.sortedDescending() shouldBeEqual listOf(5,4,3,2,1)   // List로 반환된다.
        numbers shouldBe arrayOf(5,3,2,1,4)

        val students = arrayOf(
            Score("a", 50, 60),
            Score("b", 30, 10),
            Score("c", 70, 50),
            Score("d", 100, 80)
        )
        val transform: (Score) -> String = { it.name }

        val sortedDescStudents = students.sortedByDescending { it.kor + it.eng / 2 }
        sortedDescStudents shouldBeEqual listOf(
            Score("d", 100, 80),
            Score("c", 70, 50),
            Score("a", 50, 60),
            Score("b", 30, 10)
        )
        sortedDescStudents.joinToString(",", transform = transform) shouldBe "d,c,a,b"
    }

    "정렬된 복사본 배열을 돌려주는 정렬" {
        val numbers = arrayOf(5,3,2,1,4)
        numbers.sortedArray() shouldBe arrayOf(1,2,3,4,5)
        numbers shouldBe arrayOf(5,3,2,1,4)

        numbers.sortedArrayDescending() shouldBe arrayOf(5,4,3,2,1)
        numbers shouldBe arrayOf(5,3,2,1,4)
    }

    "비교기를 사용하는 정렬" {
        val students = arrayOf(
            Score("a", 50, 60),
            Score("b", 30, 10),
            Score("c", 70, 50),
            Score("d", 100, 80)
        )
        val transform: (Score) -> String = { it.name }

        val korAscComparator : (Score, Score) -> Int = { s1,s2 -> s1.kor.compareTo(s2.kor) }
        students.sortedWith(korAscComparator) shouldBe arrayOf(
            Score("b", 30, 10),
            Score("a", 50, 60),
            Score("c", 70, 50),
            Score("d", 100, 80)
        )

        val korDescComparator : (Score, Score) -> Int = { s1,s2 -> s2.kor.compareTo(s1.kor) }
        students.sortedWith(korDescComparator) shouldBe arrayOf(
            Score("d", 100, 80),
            Score("c", 70, 50),
            Score("a", 50, 60),
            Score("b", 30, 10)
        )
    }

    /**
     * reduce - 축약 함수
     * 컬렉션의 원소 중간중간에 컬렌셕 원소 타입과 같은 타입의 인자를 둘 받은 후 같은 타입의 값을 내놓는 연산을 집어 넣어 전체 결과를 얻고 싶을 떄 사용할 수 있다.
     * 코틀린은 모든 배열과 Iterable의 하위 타입에 대해 reduce()라는 함수를 제공한다.
     */
    "reduce, reduceRight, reduceRightIndexed, reduceOrNull, reduceIndexed, reduceIndexedNull" {
        val multiple: (Int, Int) -> Int = { i1, i2 -> i1 * i2 }

        // reduce()에 람다를 넘길 떄는 보통 어느 쪽에 값이 누적되는 지를 명확히 인식할 수 있도록 acc 등의 파라미터 이름을 사용하는 경우가 일반적이다.
        val numbers = listOf(1, 2, 3, 4, 5)
        val strings = listOf("a", "b", "c", "d", "e")

        numbers.reduce(multiple) shouldBeEqual 120
        strings.reduce { acc, y -> "f($acc,$y)" } shouldBeEqual "f(f(f(f(a,b),c),d),e)"
        strings.reduce { acc, y -> "f($y,$acc)" } shouldBeEqual "f(e,f(d,f(c,f(b,a))))"
        shouldThrow<UnsupportedOperationException> { listOf<Int>().reduce { acc, y -> acc + y } }

        listOf<Int>().reduceOrNull { acc, y -> acc + y} shouldBe null

        numbers.reduceIndexed {index, acc, y -> index + acc + y} shouldBeEqual 25
        strings.reduceIndexed { index, acc, y -> "f[$index]($acc,$y)" }
            .shouldBe("f[4](f[3](f[2](f[1](a,b),c),d),e)")
        shouldThrow<UnsupportedOperationException> { listOf<Int>().reduceIndexed { index, acc, y -> index + acc + y } }


        // 일반적인 Iterable의 경우 맨 마지막 원소에 바로 접근할 수 없고 next()를 한 번 수행하고 나면 이전 원소를 다시 방문하지 못하므로
        // Iterable()의 reduceRight()는 정의되어 있지 않다.
        numbers.reduceRight { y, acc -> y + acc} shouldBeEqual 15
        strings.reduceRight { y, acc -> "f($acc,$y)"} shouldBeEqual "f(f(f(f(e,d),c),b),a)"
        strings.reduceRight { y, acc -> "f($y,$acc)"} shouldBeEqual "f(a,f(b,f(c,f(d,e))))"

        strings.reduceRightIndexed { index, acc, y -> "f[$index]($acc,$y)" }
            .shouldBe("f[0](a,f[1](b,f[2](c,f[3](d,e))))")

        // 집합은 Iterable의 하위 타입이라서 reduceRight()가 정의되어 있지 않아 List로 변환 후 사용해야 한다.
        setOf("1","2","3","4","5").toList().reduceRight {x, acc -> "f($x,$acc)"} shouldBeEqual "f(1,f(2,f(3,f(4,5))))"
        setOf("1","2","3","4","5").toList().reduceRight {x, acc -> "f($acc,$x)"} shouldBeEqual "f(f(f(f(5,4),3),2),1)"
    }

    /**
     * reduce()는 유용하지만 전달하는 람다의 두 가지 타입이 서로 호환돼야 한다는 점이다.
     * 누적값의 타입이 컬렉션 원소와 다르면 reduce()를 사용할 수 없다.
     * 하지만 누적값의 디폴트 값이 존재하고 지금까지 누적된 값과 컬렉션의 원소를 인자로 받아 새로운 누적값을 만들어주는 연산이 있다면 누적값의 타입을 컬렉션 원소와 다른 타입으로 할 수 있다.
     * (R, T) -> R
     * 이런 식의 연산을 접기 연산이라 하며, 코틀린은 배열과 Iterable의 하위 타입에 대해 접기 연산인 fold()를 제공한다.
     */
    "fold, foldIndexed, foldRight, foldRightIndexed" {
        val numbers = listOf(1,2,3,4,5)

        numbers.fold("") { acc, y -> acc + y.toString() } shouldBeEqual "12345"
        numbers.fold("") { acc, y -> "f($acc,$y)" } shouldBeEqual "f(f(f(f(f(,1),2),3),4),5)"
        numbers.fold("init") { acc, y -> acc + y.toString() } shouldBeEqual "init12345"

        numbers.foldIndexed("init") { i, acc, y -> "$acc,$y" } shouldBeEqual "init,1,2,3,4,5"
        numbers.foldIndexed("init")
            {i, acc, y -> if(i == 0) y.toString() else acc + "," + y.toString()} shouldBeEqual "1,2,3,4,5"

        numbers.foldRight("") { x, acc -> "f($x,$acc)" } shouldBeEqual "f(1,f(2,f(3,f(4,f(5,)))))"
        numbers.foldRight("") { x, acc -> "f($acc,$x)" } shouldBeEqual "f(f(f(f(f(,5),4),3),2),1)"

        numbers.foldRightIndexed("") { i, x, acc -> "f[$i]($x,$acc)" } shouldBeEqual "f[0](1,f[1](2,f[2](3,f[3](4,f[4](5,)))))"
        numbers.foldRightIndexed("") { i, x, acc -> "f[$i]($acc,$x)" } shouldBeEqual "f[0](f[1](f[2](f[3](f[4](,5),4),3),2),1)"

    }

    "joinToString" {
        val vowels = listOf("a", "e", "i", "o", "u")

        vowels.joinToString() shouldBeEqual "a, e, i, o, u"
        vowels.joinToString(",", "(", ")") shouldBeEqual "(a,e,i,o,u)"
        vowels.joinToString("|") shouldBeEqual "a|e|i|o|u"
        vowels.joinToString(prefix = "[", postfix = "]", separator = "") shouldBeEqual "[aeiou]"
        vowels.joinToString(",", "(", ")", limit = 0) shouldBeEqual "(...)"
        vowels.joinToString(",", "(", ")", limit = 1) shouldBeEqual "(a,...)"
        vowels.joinToString(",", "(", ")", limit = 4) shouldBeEqual "(a,e,i,o,...)"
        vowels.joinToString(",", "(", ")", limit = 2, truncated = "...이하생략") shouldBeEqual "(a,e,...이하생략)"

        listOf(arrayOf<Int>(), arrayOf(1,2,3)).joinToString { it.joinToString(prefix = "[", postfix = "]") }
            .shouldBe("[], [1, 2, 3]")

        val transform: (Score) -> String = {
            if (it.kor >= 80) "국어훌륭"
            else if(it.eng >= 80) "영어훌륭"
            else "보통보통"
        }
        // transform은 (T) -> CharSequence 이다.
        // 각 원소를 표시하는 데 사용할 문자열을 만들어내는 람다다.
        listOf(
            Score("첫 번째", 80,60),
            Score("두 번째", 70,90),
            Score("세 번째", 70,70),
        ).joinToString(transform = transform)
            .shouldBe("국어훌륭, 영어훌륭, 보통보통")
    }

    "flatten" {
        val sentence = "This is an example of a sentence".split(" ")
        val chars = sentence.map { it.toList() }

        chars shouldBeEqual listOf(
            listOf('T', 'h', 'i', 's'),
            listOf('i', 's'),
            listOf('a', 'n'),
            listOf('e', 'x', 'a', 'm', 'p', 'l', 'e'),
            listOf('o', 'f'),
            listOf('a'),
            listOf('s', 'e', 'n', 't', 'e', 'n', 'c', 'e')
        )

        // Array<Array<원소타입>> 이나 Iterable<Iterable<원소타입>> 인 경우에만 정의되며, 모든 원소를 펼쳐서 연결한 단일 배열이나 리스트를 돌려준다.

        chars.flatten() shouldBeEqual listOf(
            'T', 'h', 'i', 's',
            'i', 's',
            'a', 'n',
            'e', 'x', 'a', 'm', 'p', 'l', 'e',
            'o', 'f',
            'a',
            's', 'e', 'n', 't', 'e', 'n', 'c', 'e'
        )
    }

    "map, mapNotNull" {
        val array = arrayOf(1, 2, 3, 4, 5)
        val set = setOf(1, 2, 3, 4, 5)

        array.map { it * 2 } shouldBe arrayOf(2, 4, 6, 8, 10)
        set.map { it * 2 } shouldBe arrayOf(2, 4, 6, 8, 10)

        val list = listOf(1,null,2,null,3)
        list.mapNotNull { it?.times(2) } shouldBeEqual listOf(2,4,6)

        data class Person(
            var name: String,
            var age: Int
        )

        val map = mapOf(
            "one" to Person("one", 10),
            "two" to Person("two", 20),
            "three" to Person("three", 30)
        )

        map.map { it.value.age * 2 } shouldBeEqual listOf(20,40,60)
    }

    /**
     * map()에 전달하는 변환 함수의 반환 타입이 리스트 등의 컬렉션 타입인 경우가 자주 있고,
     * 그럴 때마다 flatten()을 호출하는 것은 귀찮기 때문에 코틀린은 map()과 flatten()을 합친 flatMap()을 제공한다.
     */
    "flatMap" {
        val sentence = "This is an example of a sentence"
                                    .split(" ")
                                    .flatMap { it.toList() }

        sentence shouldBeEqual listOf(
            'T', 'h', 'i', 's',
            'i', 's',
            'a', 'n',
            'e', 'x', 'a', 'm', 'p', 'l', 'e',
            'o', 'f',
            'a',
            's', 'e', 'n', 't', 'e', 'n', 'c', 'e'
        )
    }

    "map과 flatMap 차이" {
        val list = listOf("1", "2", "A", "3", "4")

        shouldThrow<NumberFormatException> { list.map(String::toInt) }

        list.map {
            try { it.toInt() } catch(e: NumberFormatException) {  }
        } shouldBe listOf(1, 2, Unit, 3, 4)

        list.flatMap {
            try { listOf(it.toInt()) } catch(e: NumberFormatException) { emptyList() }
        } shouldBe listOf(1, 2, 3, 4)
    }

    /**
     * map()에 대해서도 원소 인덱스와 원소를 함께 람다에 전달해주는 mapIndexed()가 있다.
     */
    "mapIndexed, flatMapIndexed" {
        val words = "This is an example of a sentence".split(" ")

        val charList1 = words
            .map { it.toList() }
            .flatMap {
                it.mapIndexed { index, c ->
                    if (index == 0) c.uppercaseChar()
                    else c
                }
            }

        charList1 shouldBeEqual listOf(
            'T', 'h', 'i', 's', 'I', 's', 'A', 'n', 'E', 'x', 'a', 'm', 'p', 'l', 'e', 'O', 'f', 'A', 'S', 'e', 'n', 't', 'e', 'n', 'c', 'e'
        )

        val charList2 = words.flatMapIndexed { index, wordList ->
            if (index % 2 == 0) wordList.map {it.uppercaseChar()}
            else wordList.toList() }

        charList2 shouldBeEqual listOf(
            'T', 'H', 'I', 'S', 'i', 's', 'A', 'N', 'e', 'x', 'a', 'm', 'p', 'l', 'e', 'O', 'F', 'a', 'S', 'E', 'N', 'T', 'E', 'N', 'C', 'E'
        )
    }

    "toList, toTypedArray, toSet, toMap" {
        val stringArray = arrayOf("a", "b", "c")
        val numberSet = setOf(1,2,3,4,5)
        val pairList = listOf(
            1 to "one",
            2 to "two"
        )


        stringArray.toList() shouldBe listOf("a","b","c")
        numberSet.toList() shouldBe listOf(1,2,3,4,5)

        numberSet.toTypedArray() shouldBe arrayOf(1,2,3,4,5)

        stringArray.toSet() shouldBe setOf("a","b","c")

        pairList.toMap() shouldBe mapOf(
            1 to "one",
            2 to "two"
        )
    }

    "filter, filterNot" {
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

    "filterNotNull" {
        val generateOrNull : (Int) -> Int? = { if (it % 2 == 0) null else it * 2 }
        val list = arrayOf(1,2,3,4,5).map(generateOrNull)

        list shouldBe arrayOf(2,null,6,null,10)
        list.filterNotNull() shouldBe arrayOf(2,6,10)
    }


    "indexOf, lastIndexOf, indexOfFirst, indexOfLast" {
        val list = listOf("A", "B", "C", "D", "E", "A")

        list.indexOf("A") shouldBeEqual 0
        list.indexOf("not-exist") shouldBeEqual -1

        list.lastIndexOf("A") shouldBeEqual 5
        list.lastIndexOf("E") shouldBeEqual 4
        list.lastIndexOf("not-exist") shouldBeEqual -1

        list.indexOfFirst { it == "A" } shouldBeEqual 0
        list.indexOfLast { it == "A" } shouldBeEqual 5
    }

    "sum, sumOf" {
        listOf(Int.MAX_VALUE, Int.MAX_VALUE).sum() shouldBeEqual -2
        listOf(Int.MAX_VALUE, Int.MAX_VALUE).sumOf { it.toLong() } shouldBeEqual 4294967294L
    }

    "associate" {
        val array = intArrayOf(1,2,3,4,5,6,7,8,9,10)

        array.associateBy { "A" } shouldBe mapOf("A" to 10)
        array.withIndex().associate { it.value to it.index } shouldBe
            mapOf(1 to 0, 2 to 1, 3 to 2, 4 to 3, 5 to 4, 6 to 5, 7 to 6, 8 to 7, 9 to 8, 10 to 9)
    }
})
