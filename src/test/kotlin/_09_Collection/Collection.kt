package _09_Collection

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe

class Collection : StringSpec ({

    "컬렉션의 원소가 들어있는 리스트 생성" {
        val list1: List<Set<Int>> = listOf(setOf(1,2,3,4,5))

        list1.size shouldBe 1
        list1.first().size shouldBe 5
        list1.first().first() shouldBe 1
        list1.first().last() shouldBe 5

        val list2: List<Int> = listOf(*setOf(1,2,3,4,5).toTypedArray())

        list2.size shouldBe 5
        list2.first() shouldBe 1
        list2.last() shouldBe 5
    }

    "TreeSet 을 사용해보자" {
        val treeSet = sortedSetOf("a","bb","c","d","a")

        treeSet.first() shouldBe "a"
        treeSet.last() shouldBe "d"

        val treeSet2 = sortedSetOf<String>(
            { a, b -> b.compareTo(a) },
            "a","bb","c","d","a"
        )

        treeSet2.first() shouldBe "d"
        treeSet2.last() shouldBe "a"
    }

    "Map 을 사용해보자" {
        val map = mutableMapOf(
            "one" to 1,
            "two" to 2,
            "three" to 3
        )

        map["four"] shouldBe null

        map.entries.forEachIndexed { index, item ->
            item.value shouldBe index + 1
        }
        map.keys.shouldContainAll("one","two","three")
        map.values.shouldContainAll(1,2,3)

        map.put("three", 4) shouldBe 3
        map.put("four", 4) shouldBe null

        map.remove("three", 4) shouldBe true
        map.remove("not-exist", 999) shouldBe false
    }

    "MutableCollection 을 사용해보자" {
        val mutable = mutableListOf("a", "b", "c", "d")

        mutable.remove("d") shouldBe true
        mutable.addAll(listOf("e", "f")) shouldBe true
        mutable.last() shouldBe "f"
        mutable.removeAll(listOf("a", "b")) shouldBe true
        mutable.first() shouldBe "c"
        mutable.retainAll(listOf("c", "e")) shouldBe true

        mutable.first() shouldBe "c"
        mutable.last() shouldBe "e"

        ("c" in mutable) shouldBe true
        ("e" in mutable) shouldBe true
        ("z" in mutable) shouldBe false

        mutable.addAll(listOf("e", "f"))

        val arrayList = arrayListOf("a", "b", "c")

        (mutable is ArrayList) shouldBe true
        (mutable is MutableList) shouldBe true
        (arrayList is ArrayList) shouldBe true
        (arrayList is MutableList<String>) shouldBe true
    }

    "배열과 리스트를 사용하면서 만나는 예외" {
        val array = intArrayOf()

        shouldThrow<ArrayIndexOutOfBoundsException> { array[10]  }

        val list = listOf<String>()

        shouldThrow<IndexOutOfBoundsException> { list[10]  }
        shouldThrow<NoSuchElementException> { list.first() }
        shouldThrow<NoSuchElementException> { list.last() }
    }

})
