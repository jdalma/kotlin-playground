package _30_Coroutines

import kotlinx.coroutines.*
import java.lang.Error

suspend fun main() {
//    supervisorJobTest2()
    supervisorJobTest3(listOf("a", "b"))
}

fun supervisorJobTest1() = runBlocking {
        val parentJob = SupervisorJob()
        launch(parentJob) {
            launch {
                throw Error("Some error")
            }

            launch {
                delay(2000, 0)
                println("Will not be printed")
            }
        }
        parentJob.join()
    }

fun supervisorJobTest2() = runBlocking {
    val job = SupervisorJob()
    launch(job) {
        throw Error("Some error")
    }

    launch(job) {
        delay(2000, 0)
        println("Will not be printed")
    }

    job.join()
}

suspend fun supervisorJobTest3(
    strings: List<String>
) = withContext(SupervisorJob()) {
    println(coroutineContext[Job])
    for (string in strings) {
        launch {
            println(string)
        }
    }
}
