package _50_DesginPatterns.concurrency

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.RuntimeException
import kotlin.random.Random

fun main() {
    runBlocking {
        // 1. 일반적인 값 지연 사용
        val value = valueAsync()
        println(value.await())

        // 2. Deferred 대신 Flow 사용
        val defer1 = defer { fetchUser("member1") }
        val defer2 = defer { fetchUser("member2") }

        // collect를 통해 값을 내보내야만 실제로 emit이 실행되고 출력된다.
        defer1.collect(::println)
        defer2.collect(::println)
    }
}

private suspend fun valueAsync(): Deferred<String> = coroutineScope {
    val deferred = CompletableDeferred<String>()

    launch {
        println("값 생성 시작")
        delay(3000)
        if (Random.nextBoolean()) {
            deferred.complete("OK")
        }
        else {
            deferred.completeExceptionally(
                RuntimeException()
            )
        }
    }
    deferred
}

private suspend fun fetchUser(member: String) = member

private fun defer(block: suspend () -> String): Flow<String> = flow { emit(block()) }
