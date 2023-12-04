package _30_Coroutines

import kotlin.coroutines.*

private class Cont(override val context: CoroutineContext): Continuation<Unit> {
    override fun resumeWith(result: Result<Unit>) {}
}

fun launch(ctx: CoroutineContext = EmptyCoroutineContext, block:suspend () -> Unit): Unit =
    block.startCoroutine(Cont(ctx))

suspend fun test(a: Int): Int {
    delay(1000, 0)
    return suspendCoroutine {
        it.resumeWith(Result.success(a * 2))
    }
}

fun main(args: Array<String>) {
    val start = System.currentTimeMillis()
    println("start $start")
    launch {
        println("${test(30)}")
        println("end ${System.currentTimeMillis() - start}")
    }
    Thread.sleep(2000)
}
