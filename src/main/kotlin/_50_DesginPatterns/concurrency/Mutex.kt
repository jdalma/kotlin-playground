package _50_DesginPatterns.concurrency

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

fun main() {
    runBlocking {
        var counter = 0
        val mutex = Mutex()
        val jobs = List(10) {
            async(Dispatchers.Default) {
                repeat(1000) {
                    mutex.withLock {
                        counter++
                    }
                }
            }
        }
        jobs.awaitAll()

        println(counter)
    }
}
