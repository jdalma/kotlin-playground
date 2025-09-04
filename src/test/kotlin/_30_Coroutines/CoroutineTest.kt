package _30_Coroutines

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.yield
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.suspendCoroutine
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@OptIn(InternalCoroutinesApi::class)
class CoroutineTest : StringSpec ({

    "자바의 Executor API는 충분한 성능을 내지 못한다." {
        val counter = AtomicInteger(0)
        val start = System.currentTimeMillis()
        val pool = Executors.newFixedThreadPool(100)

        for (i in 1..10_000) {
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
        for (i in 1..10_000) {
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

    "Continuation" {
        println("Before")

        suspendCoroutine<Unit> { continuation ->
            println("Before too")
            continuation.resumeWith(Result.success(Unit))
            println("After too")
        }
        Executors.newSingleThreadScheduledExecutor()
        println("After")
    }

    "Continuation 예외 전달 체인 시각화" {
        println("\n=== Continuation 예외 전달 시작 ===")

        // Continuation 체인을 만들기 위한 헬퍼 함수들
        suspend fun level1Function(): String {
            println("📍 Level 1: 시작")
            return suspendCoroutine { continuation ->
                println("📍 Level 1: suspendCoroutine 진입")

                // 다음 레벨 호출을 시뮬레이션하는 스레드
                Thread {
                    Thread.sleep(100)
                    println("📍 Level 1: 예외 발생 준비")
                    continuation.resumeWithException(
                        RuntimeException("Level 1에서 발생한 예외")
                    )
                    println("📍 Level 1: resumeWithException 호출 완료")
                }.start()
            }
        }

        suspend fun level2Function(): String {
            println("📍 Level 2: 시작")
            return try {
                level1Function()
            } catch (e: Exception) {
                println("📍 Level 2: 예외 catch - ${e.message}")
                throw RuntimeException("Level 2에서 예외를 다시 던짐: ${e.message}", e)
            }
        }

        suspend fun level3Function(): String {
            println("📍 Level 3: 시작")
            return suspendCoroutine { continuation ->
                println("📍 Level 3: suspendCoroutine 진입")

                GlobalScope.launch {
                    try {
                        val result = level2Function()
                        println("📍 Level 3: 성공적으로 완료 - $result")
                        continuation.resume(result)
                    } catch (e: Exception) {
                        println("📍 Level 3: 예외를 상위로 전달 - ${e.message}")
                        continuation.resumeWithException(e)
                    }
                }
            }
        }

        // 실제 테스트 실행
        runBlocking {
            try {
                println("🚀 메인 코루틴: 시작")
                val result = level3Function()
                println("🚀 메인 코루틴: 결과 받음 - $result")
            } catch (e: Exception) {
                println("🚀 메인 코루틴: 최종 예외 처리")
                println("   ├─ 예외 타입: ${e::class.simpleName}")
                println("   ├─ 예외 메시지: ${e.message}")
                println("   └─ 원인: ${e.cause?.message}")

                // 스택 추적을 통해 Continuation 체인 확인
                println("\n📋 스택 트레이스:")
                e.stackTrace.take(10).forEach { element ->
                    println("   └─ ${element.className}.${element.methodName}(${element.fileName}:${element.lineNumber})")
                }
            }
        }

        println("=== Continuation 예외 전달 종료 ===\n")
    }

    "Continuation 성공적인 연쇄 호출 시각화" {
        println("\n=== Continuation 성공 체인 시작 ===")

        suspend fun successLevel1(): String {
            println("✅ Level 1: 시작")
            return suspendCoroutine { continuation ->
                println("✅ Level 1: suspendCoroutine 진입")
                Thread {
                    Thread.sleep(50)
                    println("✅ Level 1: 성공 값 반환")
                    continuation.resume("Level1 결과")
                }.start()
            }
        }

        suspend fun successLevel2(): String {
            println("✅ Level 2: 시작")
            val result1 = successLevel1()
            println("✅ Level 2: Level1 결과 받음 - $result1")

            return suspendCoroutine { continuation ->
                Thread {
                    Thread.sleep(50)
                    continuation.resume("$result1 + Level2 결과")
                }.start()
            }
        }

        suspend fun successLevel3(): String {
            println("✅ Level 3: 시작")
            val result2 = successLevel2()
            println("✅ Level 3: Level2 결과 받음 - $result2")
            return "$result2 + Level3 결과"
        }

        runBlocking {
            val finalResult = successLevel3()
            println("🎉 최종 결과: $finalResult")
        }

        println("=== Continuation 성공 체인 종료 ===\n")
    }

    "throw vs resumeWithException 차이점 비교" {
        println("\n=== throw vs resumeWithException 비교 ===")

        suspend fun throwVersion(): String {
            println("🔴 throw 버전: suspendCoroutine 시작")
            return suspendCoroutine { continuation ->
                GlobalScope.launch {
                    try {
                        println("🔴 throw 버전: 예외 발생 직전")
                    } catch (e: Exception) {
                        println("🔴 throw 버전: catch에서 throw e 실행")
                        // 이렇게 하면 상위 코루틴은 영원히 기다림!
                        throw e  // ❌ continuation을 resume하지 않음
                        // continuation.resumeWithException(e) // ✅ 이것이 올바른 방법
                    }
                }
                // 여기서 continuation이 resume되지 않아서 상위 코루틴은 무한 대기
                println("🔴 throw 버전: suspendCoroutine 끝 (실행되지 않음)")
            }
        }

        suspend fun resumeWithExceptionVersion(): String {
            println("✅ resumeWithException 버전: suspendCoroutine 시작")
            return suspendCoroutine { continuation ->
                GlobalScope.launch {
                    try {
                        println("✅ resumeWithException 버전: 예외 발생 직전")
                        throw RuntimeException("내부에서 발생한 예외")
                    } catch (e: Exception) {
                        println("✅ resumeWithException 버전: catch에서 resumeWithException 실행")
                        continuation.resumeWithException(e) // ✅ 올바르게 상위로 예외 전달
                    }
                }
            }
        }

        println("1. resumeWithException 버전 테스트:")
        runBlocking {
            try {
                resumeWithExceptionVersion()
            } catch (e: Exception) {
                println("✅ 상위에서 예외 받음: ${e.message}")
            }
        }

        println("2. throw 버전 테스트 (타임아웃으로 제한):")
        runBlocking {
            try {
                kotlinx.coroutines.withTimeout(2000) {  // 2초 타임아웃
                    throwVersion()
                }
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                println("🔴 타임아웃 발생! throw 버전은 continuation을 resume하지 않아서 무한 대기됨")
            } catch (e: Exception) {
                println("🔴 예외 받음: ${e.message}")
            }
        }

        println("=== 비교 완료 ===\n")
    }

    "invokeOnCompletion 기본 동작 테스트" {
        shouldThrow<RuntimeException> {
            runBlocking {
                println("🚀 성공적으로 완료되는 코루틴 테스트")
                val successJob = launch {
                    println("✅ 작업 시작")
                    delay(100)
                    println("✅ 작업 완료")
                }

                successJob.invokeOnCompletion { throwable ->
                    if (throwable == null) {
                        println("🎉 성공적으로 완료됨!")
                    } else {
                        println("❌ 예외로 완료됨: ${throwable.message}")
                    }
                }
                successJob.join()

                println("\n🚀 예외로 실패하는 코루틴 테스트")
                val failJob = launch {
                    println("💥 작업 시작")
                    delay(50)
                    throw RuntimeException("의도적인 예외")
                }

                failJob.invokeOnCompletion { throwable ->
                    if (throwable == null) {
                        // 진입하지 않음
                    } else {
                        println("❌ 예외로 완료됨: ${throwable.message}")
                    }
                }
                try {
                    failJob.join()
                } catch (e: Exception) {
                    println("💥 예외 catch됨: ${e.message}")
                }
            }
        }
    }

    "invokeOnCompletion 취소 테스트" {

        println("\n🚀 취소되는 코루틴 테스트")
        val cancelJob = launch {
            println("🔄 작업 시작")
            try {
                repeat(10) {
                    delay(10)
                    println("🔄 작업 진행 중... $it")
                }
            } catch (e: CancellationException) {
                println("🛑 취소됨")
                throw e
            }
        }

        cancelJob.invokeOnCompletion { throwable ->
            when (throwable) {
                null -> println("🎉 성공적으로 완료됨!")
                is CancellationException -> println("🛑 취소로 완료됨: ${throwable.message}")
                else -> println("❌ 예외로 완료됨: ${throwable.message}")
            }
        }
        delay(50)
        cancelJob.cancelAndJoin()
    }

    "invokeOnCompletion 고급 기능 테스트" {
        runBlocking {
            println("🔗 여러 개의 completion handler 등록")
            val job = launch {
                println("📋 메인 작업 시작")
                delay(200)
                println("📋 메인 작업 완료")
            }

            // 첫 번째 handler
            job.invokeOnCompletion { throwable ->
                println("🥇 첫 번째 handler: ${if (throwable == null) "성공" else "실패"}")
            }

            // 두 번째 handler
            job.invokeOnCompletion { throwable ->
                println("🥈 두 번째 handler: ${if (throwable == null) "성공" else "실패"}")
            }

            // 세 번째 handler (onCancelling = true)
            job.invokeOnCompletion(onCancelling = true) { throwable ->
                println("🥉 세 번째 handler (onCancelling=true): ${if (throwable == null) "성공" else "실패"}")
            }

            job.join()

            println("\n🔧 invokeOnCompletion의 DisposableHandle 사용")
            val job2 = launch {
                println("🔧 두 번째 작업 시작")
                delay(300)
                println("🔧 두 번째 작업 완료")
            }

            val handle1 = job2.invokeOnCompletion {
                println("📝 등록된 handler 1")
            }

            val handle2 = job2.invokeOnCompletion {
                println("📝 등록된 handler 2")
            }

            // 150ms 후에 handler2 제거
            delay(150)
            handle2.dispose()
            println("🗑️ handler2 제거됨")

            job2.join()

            println("\n⚡ 이미 완료된 Job에 handler 등록")
            val completedJob = launch {
                println("⚡ 즉시 완료되는 작업")
            }

            completedJob.join() // 완료 대기

            // 이미 완료된 Job에 handler 등록
            completedJob.invokeOnCompletion { throwable ->
                println("⚡ 이미 완료된 Job에 등록된 handler: ${if (throwable == null) "성공" else "실패"}")
            }

            delay(50) // handler가 실행될 시간 제공
        }
    }

    "코루틴 예외 테스트" {
        shouldThrow<RuntimeException> {
            runBlocking {
                println("=== 내부 catch 테스트 ===")

                val job1 = launch {
                    try {
                        throw RuntimeException("내부에서 처리할 예외")
                    } catch (e: RuntimeException) {
                        println("✅ 내부에서 처리됨: ${e.message}")
                    }
                }

                val job2 = launch {
                    delay(100)
                    println("✅ 다른 작업 성공!")
                }

                joinAll(job1, job2)
                println("✅ 모든 작업 완료!")

                println("\n=== 외부 catch 테스트 ===")
                val parentJob = currentCoroutineContext().job

                try {
                    parentJob.isActive shouldBe true
                    parentJob.isCompleted shouldBe false
                    parentJob.isCancelled shouldBe false
                    val job3 = launch {
                        println("예외 발생!!!")
                        throw RuntimeException("외부로 전파될 예외") // 💥
                    }

                    val job4 = launch {
                        try {
                            delay(100)
                            println("❌ 이 메시지는 보이지 않음")
                        } catch (e: Exception) {
                            println("⚠️ 취소됨! ${e.message}")
                        }
                    }

                    job3.join()
                    job4.join()
                } catch (e: Exception) {
                    println("⚠️ 외부에서 처리됨: ${e.message}")
                }
                // 현재 코루틴의 상태를 출력
                parentJob.isActive shouldBe false
                parentJob.isCompleted shouldBe false
                parentJob.isCancelled shouldBe true
                println("⚠️ 일부 작업이 취소되었지만 프로그램은 계속")
            }
        }
    }
})
