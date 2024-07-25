package _50_DesginPatterns.concurrency

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import java.util.concurrent.Executors
import java.util.concurrent.ForkJoinPool

fun main() {
    runBlocking {

        // 이렇게 하면 부모 코루틴의 디스패처를 사용한다.
        launch {
            println("launch -> ${Thread.currentThread().name}")
        }
        launch(Dispatchers.Default) {
            println("launch(Dispatchers.Default) -> ${Thread.currentThread().name}")
        }

        async(Dispatchers.IO) {
            for (i in 1..3) {
                println("async(Dispatchers.IO) -> ${Thread.currentThread().name}")
                yield()
            }
        }

        val myDispatcher = Executors
            .newFixedThreadPool(2)
            .asCoroutineDispatcher()

        repeat(3) {
            launch(myDispatcher) {
                println("launch(myDispatcher) -> ${Thread.currentThread().name}")
            }
        }

        val forkJoinPool = ForkJoinPool(2).asCoroutineDispatcher()

        repeat(3) {
            launch(forkJoinPool) {
                println("launch(forkJoinPool) -> ${Thread.currentThread().name}")
            }
        }
    }

    // launch(Dispatchers.Default) -> DefaultDispatcher-worker-2
    // async(Dispatchers.IO) -> DefaultDispatcher-worker-1
    // async(Dispatchers.IO) -> DefaultDispatcher-worker-1
    // async(Dispatchers.IO) -> DefaultDispatcher-worker-1
    // launch(myDispatcher) -> pool-1-thread-1
    // launch(myDispatcher) -> pool-1-thread-2
    // launch(myDispatcher) -> pool-1-thread-1
    // launch(forkJoinPool) -> ForkJoinPool-1-worker-1
    // launch(forkJoinPool) -> ForkJoinPool-1-worker-2
    // launch(forkJoinPool) -> ForkJoinPool-1-worker-1
    // launch -> main
}
