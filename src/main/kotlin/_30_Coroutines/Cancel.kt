package _30_Coroutines

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlin.random.Random

suspend fun main(): Unit = coroutineScope {
    val job = Job()
    launch(job) {
        try {
            repeat(1_000) {
                delay(Random.nextLong(1000), 1)
                println("repeat $it")
            }
        } catch (e: CancellationException) {
            println(e)
            throw e
        }
    }
    delay(2_000, 1)
    job.cancelAndJoin()
    println("cancel")
}
