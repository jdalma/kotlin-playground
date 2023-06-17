package _10_String

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.equals.shouldBeEqual

class StringTest : StringSpec ({

    "trimMargin" {
        val text = """
            |세로 막대를 기준으로 각 줄의 위치를 정렬한다. 
            |첫 번째 라인,
            |두 번째 라인,
            |세 번째 라인
        """.trimMargin()
        text shouldBeEqual
                "세로 막대를 기준으로 각 줄의 위치를 정렬한다. \n" +
                "첫 번째 라인,\n" +
                "두 번째 라인,\n" +
                "세 번째 라인"

        val text2 = """
            #샵을 기준으로 각 줄의 위치를 정렬한다. 
            #첫 번째 라인,
            #두 번째 라인,
            #세 번째 라인
        """.trimMargin("#")

        text2 shouldBeEqual  "샵을 기준으로 각 줄의 위치를 정렬한다. \n" +
                "첫 번째 라인,\n" +
                "두 번째 라인,\n" +
                "세 번째 라인"
    }

    "trimIndent" {
        val text = """
            공통 들여쓰기를 기준으로 줄이 정렬된다.
                    두 번
                    
                한 번
        """.trimIndent()

        text shouldBeEqual "공통 들여쓰기를 기준으로 줄이 정렬된다.\n" +
                "        두 번\n" +
                "        \n" +
                "    한 번"
    }
})
