package _30_Coroutines

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.equals.shouldBeEqual
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import java.lang.RuntimeException

class FlowTest: StringSpec ({

    "구독/게시 테스트" {
        val numbersFlow: Flow<Int> = flow {
            (0 .. 10).forEach {
                if (it == 9) {
                    println("$it 예외 발생")
                    throw RuntimeException()
                }
                println("$it 전송 중")
                emit(it)
            }
        }

        var prev = 0
        shouldThrow<RuntimeException> {
            numbersFlow.collect { number ->
                println("$number 수신")
                prev++ shouldBeEqual number
            }
        }
    }

    "여러 소비자가가 하나의 생산자에게서 값을 읽기" {
        var publishCount = 0
        val numbersFlow: Flow<Int> = flow {
            (1 .. 30).forEach {
                println("$it 전송 중")
                publishCount++
                emit(it)
            }
        }

        var consumerCount = 0
        launch(Dispatchers.Default) {
            numbersFlow.collect { number ->
                consumerCount++
                println("1번 코루틴에서 $number 수신")
            }
        }

        launch(Dispatchers.Default) {
            numbersFlow.collect { number ->
                consumerCount++
                println("2번 코루틴에서 $number 수신")
            }
        }

        launch(Dispatchers.Default) {
            numbersFlow.collect { number ->
                consumerCount++
                println("3번 코루틴에서 $number 수신")
            }
        }

        // last()를 호출하면 새로운 컨슈머를 사용하는 것과 동일하게 데이터를 처음부터 끝까지 받게 되며 데이터의 공급이 끝날 때 까지 기다린다.
        val last = numbersFlow.last().also {
            println("last 소비자에서 $it 수신")
        }
        last shouldBeEqual 30

        delay(10000)

        publishCount shouldBeEqual 120
        consumerCount shouldBeEqual 90
    }

    "버퍼 있는 흐름" {
        var publishCount = 0
        val numbersFlow: Flow<Int> = flow {
            (0 .. 10).forEach {
                println("$it 전송 중")
                publishCount++
                emit(it)
            }
        }

        var consumerCount = 0
        (1..8).forEach { coroutineId ->
            launch(Dispatchers.Default) {
                numbersFlow.buffer().collect { number ->
                    consumerCount++
                    println("$coroutineId 번 코루틴에서 $number 수신")
                }
            }
        }

        delay(3000)
        publishCount shouldBeEqual 88
        consumerCount shouldBeEqual 88
    }

    "흐름 뭉개기" {
        val stock: Flow<Int> = flow {
            var i = 1
            repeat(100) {
                println("$i 생산")
                emit(i++)
                delay(100)
            }
        }

        var seconds = 0
        stock.conflate().collect { number ->
            delay(1000)
            seconds++
            println("${seconds}초 -> $number 소모")
        }
    }

    "쌍방 중단" {
        fun dispatcherSeparateFlow() = flow {
            for (i in 1..5) {
                delay(100)
                println("emit : [$i] - ${Thread.currentThread().name}")
                emit(i)
            }
        }

        dispatcherSeparateFlow().flowOn(Dispatchers.IO).collect {
            println("collect : [$it] - ${Thread.currentThread().name}")
        }

        // emit : [1] - DefaultDispatcher-worker-1
        // collect : [1] - pool-1-thread-1
        // emit : [2] - DefaultDispatcher-worker-1
        // collect : [2] - pool-1-thread-1
        // ...
    }
})
