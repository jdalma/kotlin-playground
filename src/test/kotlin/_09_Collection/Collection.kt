package _09_Collection

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import java.util.PriorityQueue

class Collection : StringSpec ({

    "컬렉션의 원소가 들어있는 리스트 생성" {
        val list1: List<Set<Int>> = listOf(setOf(1,2,3,4,5))

        list1.size shouldBeEqual 1
        list1.first().size shouldBeEqual 5
        list1.first().first() shouldBeEqual 1
        list1.first().last() shouldBeEqual 5

        val list2: List<Int> = listOf(*setOf(1,2,3,4,5).toTypedArray())

        list2.size shouldBeEqual 5
        list2.first() shouldBeEqual 1
        list2.last() shouldBeEqual 5
    }

    "TreeSet 을 사용해보자" {
        val treeSet = sortedSetOf("a","bb","c","d","a")

        treeSet.first() shouldBeEqual "a"
        treeSet.last() shouldBeEqual "d"

        val treeSet2 = sortedSetOf<String>(
            { a, b -> b.compareTo(a) },
            "a","bb","c","d","a"
        )

        treeSet2.first() shouldBeEqual "d"
        treeSet2.last() shouldBeEqual "a"
    }

    "Map 을 사용해보자" {
        val map = mutableMapOf(
            "one" to 1,
            "two" to 2,
            "three" to 3
        )

        map["four"] shouldBe null

        map.entries.forEachIndexed { index, item ->
            item.value shouldBeEqual index + 1
        }
        map.keys.shouldContainAll("one","two","three")
        map.values.shouldContainAll(1,2,3)

        map.put("three", 4)?.shouldBeEqual(3)
        map.put("four", 4) shouldBe null

        map.remove("three", 4) shouldBeEqual true
        map.remove("not-exist", 999) shouldBeEqual false
    }

    "MutableCollection 을 사용해보자" {
        val mutable = mutableListOf("a", "b", "c", "d")

        mutable.remove("d") shouldBeEqual true
        mutable.addAll(listOf("e", "f")) shouldBeEqual true
        mutable.last() shouldBeEqual "f"
        mutable.removeAll(listOf("a", "b")) shouldBeEqual true
        mutable.first() shouldBeEqual "c"
        mutable.retainAll(listOf("c", "e")) shouldBeEqual true

        mutable.first() shouldBeEqual "c"
        mutable.last() shouldBeEqual "e"

        ("c" in mutable) shouldBeEqual true
        ("e" in mutable) shouldBeEqual true
        ("z" in mutable) shouldBeEqual false

        mutable.addAll(listOf("e", "f"))

        val arrayList = arrayListOf("a", "b", "c")

        (mutable is ArrayList) shouldBeEqual true
        (mutable is MutableList) shouldBeEqual true
        (arrayList is ArrayList) shouldBeEqual true
        (arrayList is MutableList<String>) shouldBeEqual true
    }

    "배열과 리스트를 사용하면서 만나는 예외" {
        val array = intArrayOf()

        shouldThrow<ArrayIndexOutOfBoundsException> { array[10]  }

        val list = listOf<String>()

        shouldThrow<IndexOutOfBoundsException> { list[10]  }
        shouldThrow<NoSuchElementException> { list.first() }
        shouldThrow<NoSuchElementException> { list.last() }
    }

    "우선순위 큐" {
        val maxHeap: PriorityQueue<String> = PriorityQueue<String> {
                v1, v2 -> v2.compareTo(v1)
        }

        maxHeap.offer("A")
        maxHeap.offer("A")
        maxHeap.offer("Z")
        maxHeap.offer("D")

        maxHeap.poll() shouldBeEqual "Z"
        maxHeap.size shouldBeEqual 3
    }
})
