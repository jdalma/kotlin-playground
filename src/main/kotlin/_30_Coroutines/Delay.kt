package _30_Coroutines

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private val executor = Executors.newSingleThreadScheduledExecutor {
    Thread(it, "scheduler").apply { isDaemon = true }
}

suspend fun delay(timeMillis: Long, number: Int): Int =
    suspendCoroutine { cont ->
        executor.schedule({
            cont.resume(number)
        }, timeMillis, TimeUnit.MILLISECONDS)
    }

suspend fun main() {
    println("before")

    (0..10).forEachIndexed { index, e ->
        println(delay(3000, index))
    }

    println("after")
}
