package _04_ClassAndObject

class StudentScore(
    val name: String,
    val kor: Int,
    val eng: Int,
    val math: Int
) {
    fun sum() = kor + eng + math
    fun average() : Double {
        return sum() / 3.0
    }
}

class InitExample(
    val x: String = ""
) {
    val len = x.length

    init {
        println("x value=$x")
    }

    var result: String = ""

    init {
        result = "$x:$len"
    }
}

class Foo(
    title: String
) {

    val title = "$title!"
    var title2 : String

    fun allTitle() : String = "${title}_$title2"

    init {
        title2 = title
    }
}
