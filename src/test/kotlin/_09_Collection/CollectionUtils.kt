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

})
