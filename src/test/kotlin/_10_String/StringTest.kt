package _10_String

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe

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


//        val text3 = """
//            첫 번째 줄
//             두 번째 줄
//        """.trimMargin(" ") // java.lang.IllegalArgumentException: marginPrefix must be non-blank string.
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

        val text2 = """
                    최소 들여쓰기 기준으로
                        줄이 정렬된다.
                    다음 줄
                그 다음 줄
        """.trimIndent()

        text2 shouldBeEqual "    최소 들여쓰기 기준으로\n" +
                "        줄이 정렬된다.\n" +
                "    다음 줄\n" +
                "그 다음 줄"
    }

    "문자열 비교" {
        val test1 = "test"
        val test2 = "TEST"

        test1.equals(test2).shouldBeFalse()
        test1.equals(test2, false).shouldBeFalse()
        test1.equals(test2, true).shouldBeTrue()
    }

    "문자열 대소 비교" {
        ("0" < "9").shouldBeTrue()
        ("9" < "A").shouldBeTrue()
        ("A" < "a").shouldBeTrue()
        ("Aa" < "aA").shouldBeTrue()
        ("Aa" == "aA").shouldBeFalse()
        ("AAA" < "AAAA").shouldBeTrue()
        ("filename12" < "filename9").shouldBeTrue()
        ("filename12" < "filename02").shouldBeFalse()

        ("0".compareTo("9")) shouldBe -9
        ("9".compareTo("0")) shouldBe 9
        ("Aa".compareTo("aA")) shouldBe -32
        ("AA".compareTo("aa")) shouldBe -32
        ("AA".compareTo("aa", true)) shouldBe 0
    }

    "유니코드 정규화" {
        val denormal = "\u1112\u1161\u11AB"
        val normal = "\uD55C"

        (denormal + normal) shouldBe "한한"
        (denormal == normal) shouldBe false

        java.text.Normalizer.isNormalized(normal, java.text.Normalizer.Form.NFC) shouldBe true
        java.text.Normalizer.isNormalized(denormal, java.text.Normalizer.Form.NFC) shouldBe false
        java.text.Normalizer.isNormalized(normal, java.text.Normalizer.Form.NFD) shouldBe false
        java.text.Normalizer.isNormalized(denormal, java.text.Normalizer.Form.NFD) shouldBe true

        (java.text.Normalizer.normalize(normal, java.text.Normalizer.Form.NFD) == denormal) shouldBe true
        (java.text.Normalizer.normalize(denormal, java.text.Normalizer.Form.NFC) == normal) shouldBe true
    }

    "부분 문자열" {
        // 첫 번째 인자는 문자열의 시작 위치
        // 두 번째 인자는 문자열의 끝나는 위치
        "01234567890".substring(0,1) shouldBeEqual "0"
        "01234567890".substring(1,6) shouldBeEqual "12345"
        "01234567890".substring(3,3) shouldBeEqual ""

        shouldThrow<StringIndexOutOfBoundsException> { "01234567890".substring(10,20) }
    }

    "문자열 치환" {
        val text = "My name is jeongdalMa"

        text.replace("", "-") shouldBeEqual
                "-M-y- -n-a-m-e- -i-s- -j-e-o-n-g-d-a-l-M-a-"

        text.replace("m", "m!", true) shouldBeEqual
                "m!y nam!e is jeongdalm!a"

        text.replace("m", "m!", false) shouldBeEqual
                "My nam!e is jeongdalMa"
    }

    "문자열 형싟화" {
        val test = "W %.4f".format(10.0 / 3.0)

        test shouldBeEqual "W 3.3333"

        val name = "Alice"
        val age = 25
        val weight = 62.5
        val isStudent = true
        val initial = 'A'

        "Name: %s\nAge: %d\nWeight: %.1f\nIs Student: %b\nInitial: %c".format(name, age, weight, isStudent, initial) shouldBeEqual
                "Name: Alice\nAge: 25\nWeight: 62.5\nIs Student: true\nInitial: A"

        "%10.5f".format(1.23456e2) shouldBeEqual " 123.45600"
        "%10.5g".format(1.23456e2) shouldBeEqual "    123.46"
        "%15.5g".format(1.23456e-10) shouldBeEqual "     1.2346e-10"
    }
})
