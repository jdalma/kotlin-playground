package leetcode_study

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.util.ArrayDeque

/**
 * 문제를 읽고 바로 BFS를 떠올려서 실행했지만 엣지케이스를 통과하지 못함
 * 방문 체크에 대한 백트래킹이 필요하지만 BFS를 통한 백트래킹은 까다로움
 */
class `word-search-fail` {

    fun exist(board: Array<CharArray>, word: String): Boolean {
        return usingBfs(board, word)
    }

    private fun usingBfs(board: Array<CharArray>, word: String): Boolean {
        for (x in board.indices) {
            for (y in board[x].indices) {
                if (board[x][y] == word[0] && findWord(board, word, Position(x,y, 1))) {
                    return true
                }
            }
        }
        return false
    }

    private fun findWord(board: Array<CharArray>, word: String, position: Position): Boolean {
        val visited = Array(board.size) {
            BooleanArray(board[0].size)
        }

        val queue = ArrayDeque<Position>().apply {
            this.add(position)
        }

        while (queue.isNotEmpty()) {
            val now = queue.poll()
            if (now.count == word.length) return true
            visited[now.x][now.y] = true
            val nextCharPositions = now.findNextCharPositions(board, visited, word[now.count])
            nextCharPositions.forEach {
                queue.offer(it)
            }
        }
        return false
    }

    @Test
    fun `문자로 구성된 2차원 배열에서 word 문자열 존재 유무를 반환한다`() {
        exist(arrayOf(
                charArrayOf('A','B','C','E'),
                charArrayOf('S','F','C','S'),
                charArrayOf('A','D','E','E')
            ), "ABCCED") shouldBe true
        exist(arrayOf(
            charArrayOf('A','B','C','E'),
            charArrayOf('S','F','C','S'),
            charArrayOf('A','D','E','E')
        ), "SEE") shouldBe true
        exist(arrayOf(
            charArrayOf('A','B','C','E'),
            charArrayOf('S','F','C','S'),
            charArrayOf('A','D','E','E')
        ), "SES") shouldBe false
        exist(arrayOf(
            charArrayOf('A','B','C','E'),
            charArrayOf('S','F','E','S'),
            charArrayOf('A','D','E','E')
        ), "ABCESEEEFS") shouldBe true
    }

    data class Position(
        val x: Int,
        val y: Int,
        val count: Int
    ) {

        operator fun plus(other: Position) = Position(this.x + other.x, this.y + other.y, this.count + 1)

        fun findNextCharPositions(board: Array<CharArray>, visited: Array<BooleanArray>, char: Char): List<Position> {
            return MOVES.map { this + it }
                .filter { it.isNotOutOfIndexed(board) && !visited[it.x][it.y] }
                .filter { board[it.x][it.y] == char }
        }

        private fun isNotOutOfIndexed(board: Array<CharArray>) =
            this.x < board.size && this.x >= 0 && this.y < board[0].size &&  this.y >= 0

        companion object {
            private val MOVES: List<Position> = listOf(
                Position(-1, 0, 0),
                Position(0, 1, 0),
                Position(1, 0, 0),
                Position(0, -1, 0),
            )
        }
    }
}
