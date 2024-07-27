package _30_Coroutines

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.equals.shouldBeEqual
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChannelTest: StringSpec ({

    "채널을 사용한 순차 프로세스 통신" {
        val onlyIntChannel = Channel<Int>()

        launch {
            var number = 1
            for (c in onlyIntChannel) {
                number++ shouldBeEqual c
            }
        }

        (1 .. 10).forEach {
            onlyIntChannel.send(it)
        }
        onlyIntChannel.close()
    }

    "채널에 공급된 데이터는 동시성에 노출된다," {
        val channel = produce {
            (1 .. 30).forEach {
                println("$it 생산")
                delay(500)
                send(it)
            }
        }

        // 이터레이터를 통한 컨슘은 데이터가 공급되면 계속 소비한다.
        // 공급이 끊기면 이터레이터도 끝난다.
        launch {
            launch {
                for (c in channel) {
                    println("iterator1 : ${Thread.currentThread().name} $c")
                }
                println("1번 컨슈머 종료")
            }
            for (c in channel) {
                println("iterator2 : ${Thread.currentThread().name} $c")
            }
            println("2번 컨슈머 종료")
        }

        delay(5000)

        // 5초 후에 새로 합류하는 소비자는 Flow와 다르게 공급되는 데이터부터 소비를 시작한다.
        GlobalScope.launch {
            channel.consumeEach {
                println("consumeEach : ${Thread.currentThread().name} $it")
            }
            println("3번 컨슈머 종료")
        }
        // iterator2 : pool-1-thread-1 2
        // consumeEach : DefaultDispatcher-worker-1 1
        // iterator2 : pool-1-thread-1 3
        // consumeEach : DefaultDispatcher-worker-1 4
        // iterator2 : pool-1-thread-1 6
        // iterator1 : pool-1-thread-1 5
        // consumeEach : DefaultDispatcher-worker-1 7
        // iterator2 : pool-1-thread-1 8
        // consumeEach : DefaultDispatcher-worker-1 10
        // iterator1 : pool-1-thread-1 9
    }

    "생산자 코루틴" {
        // produce 함수를 사용해 생산자 코루틴을 만들 수 있다.
        // 공급하고자 하는 값의 타입이 T라고 할 때 produce 함수는 내부적으로 ReceiveChannel<T>를 갖고 있는 코루틴을 생성한다.
        val channel = produce {
            (1 .. 10).forEach {
                send(it)
            }
        }

        launch {
            var number = 1
            for (c in channel) {
                number++ shouldBeEqual c
            }
        }
    }

    "행위자 코루틴" {
        // actor 함수도 produce와 마찬가지로 내부에 채널을 갖고 있는 코루틴을 생성한다.
        // 행위자는 채널에서 값을 가져오는 역할을 한다.
        var executeCount = 0
        val actor = actor {
            var number = 1
            channel.consumeEach {
                executeCount++
                number++ shouldBeEqual it
            }
        }

        (1 .. 10).forEach {
            println(it)
            actor.send(it)
        }

        (11..20).forEach {
            println(it)
            actor.send(it)
        }

        actor.close()
    }

    "버퍼가 있는 채널" {
        // 채널이 다음 값을 받을 준비가 될 때까지 생산자가 멈춰 있는 것을 볼 수 있다.
        // 즉. actor 객체가 자신이 준비될 때까지 다음 값을 전송하지 말라는 의미의 배압을 생산자에게 가하는 것이다.
        // 용량의 기본 값은 0이며, 채널에 들어 있는 값이 소비되기 전까지는 아무도 채널에 새로운 값을 전송할 수 없다는 것이다.
        // 용량이 정해져 있지 않았을 때는 100ms 씩 차이가 났지만 용량을 10으로 정하면 타임스탬프의 시간이 차이 나지 않는다.
        val actor = actor<Long>(capacity = 10) {
            var prev = 0L
            channel.consumeEach {
                println(it - prev)
                prev = it
                delay(100)
            }
        }

        repeat(10) {
            actor.send(System.currentTimeMillis())
        }

        actor.close().also {
            println("전송 완료")
        }
    }
})
