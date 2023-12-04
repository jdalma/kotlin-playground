package _30_Coroutines

import kotlinx.coroutines.*

fun main(): Unit = runBlocking {
    val handler = CoroutineExceptionHandler { ctx, exception ->
        println("Caught $exception")
    }

    val scope = CoroutineScope(SupervisorJob() + handler)
    scope.launch {
        delay(1000, 1)
        throw Error("Some error")
    }

    scope.launch {
        delay(2000, 1)
        println("Will be printed")
    }

    delay(3000, 0)
}
