package _09_Collection

class Score (
    val name: String,
    val kor: Int,
    val eng: Int
): Comparable<Score> {
    override fun compareTo(other: Score): Int =
        this.name.compareTo(other.name)

    override fun toString(): String {
        return "Score(name=\"$name\", kor=$kor, eng=$eng)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Score

        if (name != other.name) return false
        if (kor != other.kor) return false
        return eng == other.eng
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + kor
        result = 31 * result + eng
        return result
    }
}
