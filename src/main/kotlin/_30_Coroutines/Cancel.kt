package _30_Coroutines

import kotlinx.coroutines.*
import kotlin.random.Random

fun main() {
    runBlocking {
//        loopTest()
//        loopTest2()
        loopTest3()
        println("canceled main")
    }
}

suspend fun test() = coroutineScope {
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

suspend fun loopTest() = coroutineScope {
    val job = launch {
        repeat(10) { i ->
            println("repeat $i ...")
            try {
                delay(10L)
            } catch (e: CancellationException) {
                println("catch $e")
            }
        }
    }
    delay(55L)
    job.cancelAndJoin()
    println("canceled loopTest")

//    repeat 0 ...
//    repeat 1 ...
//    repeat 2 ...
//    repeat 3 ...
//    repeat 4 ...
//    catch kotlinx.coroutines.JobCancellationException: StandaloneCoroutine was cancelled; job=StandaloneCoroutine{Cancelling}@769c9116
//    repeat 5 ...
//    catch kotlinx.coroutines.JobCancellationException: StandaloneCoroutine was cancelled; job=StandaloneCoroutine{Cancelling}@769c9116
//    repeat 6 ...
//    catch kotlinx.coroutines.JobCancellationException: StandaloneCoroutine was cancelled; job=StandaloneCoroutine{Cancelling}@769c9116
//    repeat 7 ...
//    catch kotlinx.coroutines.JobCancellationException: StandaloneCoroutine was cancelled; job=StandaloneCoroutine{Cancelling}@769c9116
//    repeat 8 ...
//    catch kotlinx.coroutines.JobCancellationException: StandaloneCoroutine was cancelled; job=StandaloneCoroutine{Cancelling}@769c9116
//    repeat 9 ...
//    catch kotlinx.coroutines.JobCancellationException: StandaloneCoroutine was cancelled; job=StandaloneCoroutine{Cancelling}@769c9116
//    canceled loopTest
//    canceled main
}


suspend fun loopTest2() = coroutineScope {
    val job = launch {
        repeat(10) { i ->
            println("repeat $i ...")
            try {
                delay(10L)
            } catch (e: CancellationException) {
                println("catch $e")
            }
        }
    }
    val handle = job.invokeOnCompletion {
        println("invokeOnCompletion $it")
    }

    delay(55L)
    job.cancelAndJoin()
    println("canceled loopTest")

//    repeat 0 ...
//    repeat 1 ...
//    repeat 2 ...
//    repeat 3 ...
//    repeat 4 ...
//    catch kotlinx.coroutines.JobCancellationException: StandaloneCoroutine was cancelled; job=StandaloneCoroutine{Cancelling}@12bc6874
//    repeat 5 ...
//    catch kotlinx.coroutines.JobCancellationException: StandaloneCoroutine was cancelled; job=StandaloneCoroutine{Cancelling}@12bc6874
//    repeat 6 ...
//    catch kotlinx.coroutines.JobCancellationException: StandaloneCoroutine was cancelled; job=StandaloneCoroutine{Cancelling}@12bc6874
//    repeat 7 ...
//    catch kotlinx.coroutines.JobCancellationException: StandaloneCoroutine was cancelled; job=StandaloneCoroutine{Cancelling}@12bc6874
//    repeat 8 ...
//    catch kotlinx.coroutines.JobCancellationException: StandaloneCoroutine was cancelled; job=StandaloneCoroutine{Cancelling}@12bc6874
//    repeat 9 ...
//    catch kotlinx.coroutines.JobCancellationException: StandaloneCoroutine was cancelled; job=StandaloneCoroutine{Cancelling}@12bc6874
//    invokeOnCompletion kotlinx.coroutines.JobCancellationException: StandaloneCoroutine was cancelled; job=StandaloneCoroutine{Cancelled}@12bc6874
//    canceled loopTest
//    canceled main
}

suspend fun loopTest3() = coroutineScope {
    val job = launch {
        try {
            repeat(10) { i ->
                println("repeat $i ...")
                delay(10L)
            }
        }
        catch (e: CancellationException) {
            println("catch $e")
        }
    }
    val handle = job.invokeOnCompletion {
        println("invokeOnCompletion $it")
    }
    delay(55L)
    job.cancelAndJoin()
    println("canceled loopTest")

//    repeat 0 ...
//    repeat 1 ...
//    repeat 2 ...
//    repeat 3 ...
//    repeat 4 ...
//    catch kotlinx.coroutines.JobCancellationException: StandaloneCoroutine was cancelled; job=StandaloneCoroutine{Cancelling}@12bc6874
//    invokeOnCompletion kotlinx.coroutines.JobCancellationException: StandaloneCoroutine was cancelled; job=StandaloneCoroutine{Cancelled}@12bc6874
//    canceled loopTest
//    canceled main
}
