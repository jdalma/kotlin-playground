package _30_Coroutines

import kotlinx.coroutines.*

suspend fun main(): Unit = coroutineScope {
    val job = Job()
    launch(job) {
        delay(1000, 1)
        println("Text 1")
    }

    launch(job) {
        delay(2000, 1)
        println("Text 2")
    }
    job.children.forEach { it.join() }
    job.complete()

    launch {
        while(coroutineContext[Job]?.isCompleted == false) {
            delay(1000, 0)
        }
    }
}
