package _30_Coroutines

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

fun main() = runBlocking(CoroutineName("Parent")) {
    log("Before")

    coroutineScope {
        delay(1000, 0)
        log("Hello 0")
    }

    withContext(CoroutineName("Child1")) {
        delay(1000, 0)
        log("Hello 1")
    }

    withContext(CoroutineName("Child2")) {
        delay(1000, 0)
        log("Hello 2")
    }

    log("After")
}
