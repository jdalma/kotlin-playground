package _30_Coroutines

import io.kotest.core.spec.style.StringSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.yield
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.cancellation.CancellationException

class CoroutineTest : StringSpec ({

    "자바의 Executor API는 충분한 성능을 내지 못한다." {
        val counter = AtomicInteger(0)
        val start = System.currentTimeMillis()
        val pool = Executors.newFixedThreadPool(100)

        for ( i in 1 .. 10_000) {
            pool.submit {
                counter.incrementAndGet();
                Thread.sleep(100)
                counter.incrementAndGet();
            }
        }

        pool.awaitTermination(20, TimeUnit.SECONDS)
        pool.shutdown()
        println("${System.currentTimeMillis() - start} 밀리초 동안 ${counter.get() / 2} 개의 작업을 완료함")
    }

    "코루틴 시작하기" {
        val latch = CountDownLatch(10_000)
        val c = AtomicInteger()
        val start = System.currentTimeMillis()
        for (i in 1 .. 10_000) {
            GlobalScope.launch {
                c.incrementAndGet()
                delay(100)
                c.incrementAndGet()
                latch.countDown()
            }
        }
        latch.await(10, TimeUnit.SECONDS)
        println("${System.currentTimeMillis() - start} 밀리초 동안 ${c.get() / 2} 개의 작업을 완료함")
    }

    "반환값을 받기위한 async 함수 사용하기" {
        fun fastUuidAsync() = GlobalScope.async {
            UUID.randomUUID()
        }
        val job = fastUuidAsync()
        println(job.await())
    }

    "작업 취소해보기" {
        val cancellable = GlobalScope.launch {
            try {
                for (i in 1..1000) {
                    // 취소 가능 코루틴을 취소해도 즉시 취소되지 않는다.
                    println("취소 가능: $i")
                    if (i % 100 == 0) {
                        yield()
                    }
                }
            } catch (e: CancellationException) {
                e.printStackTrace()
            }
        }

        val notCancellable = GlobalScope.launch {
            for (i in 1..10_000) {
                if (i % 100 == 0) {
                    println("취소 불가능 $i")
                }
            }
        }

        println("취소 가능 코루틴을 취소 중")
        cancellable.cancel()
        println("취소 불가능 코루틴을 취소 중")
        notCancellable.cancel()

        cancellable.join()
        notCancellable.join()
    }

    "자식 코루틴을 끝까지 기다리지 않는다." {
        runBlocking {
            val parent = launch(Dispatchers.Default) {
                supervisorScope {
                    val children = List(10) { childId ->
                        launch {
                            for (i in 1..1_000_000) {
                                UUID.randomUUID()

                                if (i % 1_000_000 == 0) {
                                    println("$childId - $i")
                                    yield()
                                }
                                if (childId == 8) {
                                    throw RuntimeException("Something bad happened")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
})
