package _50_DesginPatterns.concurrency

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

fun main() {
    runBlocking {
        // 1. 데이터 클래스로 장벽 세우기
        val favoriteCharacter = fetchFavoriteCharacterCorrect("데이터 클래스로 장벽 세우기")
        println(favoriteCharacter)

        // 2. 동일한 타입을 반환하는 일시 중단 함수를 동시에 실행하기
        val characters: List<Deferred<FavoriteCharacter>> = listOf(
            Me.getFavoriteCharacter(),
            Taylor.getFavoriteCharacter(),
            Michael.getFavoriteCharacter()
        )
        println(characters.awaitAll())

        // 3. 일시 중단 함수를 동시에 실행할 때 flatMapMerge, flattenMerge 활용
        val defer1 = defer { fetchUser("member1") }
        val defer2 = defer { fetchUser("member2") }
        // toList를 호출해야 실제로 emit된다.
        val members = flowOf(defer1, defer2).flattenMerge().toList()
        println(members)
    }
}

private suspend fun fetchUser(member: String) = member

private fun defer(block: suspend () -> String): Flow<String> = flow { emit(block()) }

data class FavoriteCharacter(
    val name: String,
    val catchphrase: String,
    val picture: ByteArray = Random.nextBytes(42)
)

fun CoroutineScope.getCatchphraseAsync(
    characterName: String
) = async {
    delay(1000)
    "Hello. My name is Inigo Montoya. You killed my father. Prepare to die."
}

fun CoroutineScope.getPicture(
    characterName: String
) = async {
    delay(2000)
    Random.nextBytes(42)
}

suspend fun fetchFavoriteCharacterCorrect(
    name: String
) = coroutineScope {
    val catchphrase = getCatchphraseAsync(name)
    val picture = getPicture(name)

    FavoriteCharacter(name, catchphrase.await(), picture.await())
}

object Michael {
    suspend fun getFavoriteCharacter() = coroutineScope {
        async {
            FavoriteCharacter("터미네이터", "Hasta la vista, baby")
        }
    }
}

object Taylor {
    suspend fun getFavoriteCharacter() = coroutineScope {
        async {
            FavoriteCharacter("돈 비토 코를레오네", "그 자에게 절대 거절할 수 없는 제안을 하겠다.")
        }
    }
}

object Me {
    suspend fun getFavoriteCharacter() = coroutineScope {
        async {
            FavoriteCharacter("이니고 몬토야", "안녕, 난 이니고 몬토야다.")
        }
    }
}
