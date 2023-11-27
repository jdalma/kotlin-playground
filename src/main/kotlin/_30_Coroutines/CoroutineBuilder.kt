package _30_Coroutines

import kotlinx.coroutines.*

fun CoroutineScope.log(msg: String) {
    val name = this.coroutineContext[CoroutineName]
    println(coroutineContext[CoroutineName]?.name)
    println(coroutineContext[Job]?.isActive)
}

fun main() {
    runBlocking(CoroutineName("main")) {
        log("Started")
        val v1 = async(CoroutineName("c1")) {
            delay(500)
            log("Running Async")
            42
        }
        log("The answer is ${v1.await()}")
        println(coroutineContext)
    }
}

suspend fun suspendHello() {
    delay(1000, 0)
    println("Suspend Hello")
}

fun hello() {
    println("Hello")
}
// 코루틴 스코프는 async, launch, yield 를 사용할 수 있는 어휘를 제공한다.
// runBlocking 은 job join이 내장되어 있다.
// coroutineScope 는 컨티뉴에이션을 감추고 우아하게 사용할 수 있도록 지원하는 것이다.
