package _50_DesginPatterns.concurrency

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.RuntimeException
import kotlin.random.Random

fun main() {
    runBlocking {
        val value = valueAsync()
        println(value.await())
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
