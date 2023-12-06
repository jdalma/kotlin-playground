package _30_Coroutines

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

suspend fun main() = coroutineScope {
    val test = async {
        delay(1000, 0)
        10
    }

    val result = test.await()
    println("start 0")
    launch { show2() }
    show()
    println("end")
}

fun show() {
    Thread.sleep(6000)
}

suspend fun show2() {
    delay(5000, 0)
    println("start 1")
}
