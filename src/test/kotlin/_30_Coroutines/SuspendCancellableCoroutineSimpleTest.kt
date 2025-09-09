package _30_Coroutines

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * suspendCancellableCoroutine의 핵심 특징과 이점을 간단히 검증
 * 
 * Kotlin Coroutine Expert 관점:
 * - 콜백 → 코루틴 브릿지 역할
 * - 취소 지원으로 리소스 누수 방지
 * - 일반 suspendCoroutine과의 차이점
 * 
 * Unit Test Expert 관점:
 * - 실제 동작하는 간단한 예제로 이점 확인
 */
class SuspendCancellableCoroutineSimpleTest : BehaviorSpec({

    given("첫 번째 핵심 이점: 콜백 → 코루틴 변환") {
        `when`("콜백 기반 API를 코루틴으로 래핑하면") {
            then("자연스러운 동기식 코드 작성이 가능하다") {
                
                // 콜백 기반 레거시 API
                class LegacyNetworkClient {
                    fun fetchData(callback: (String?, Exception?) -> Unit) {
                        // 비동기 작업 시뮬레이션
                        Thread {
                            Thread.sleep(100)
                            callback("서버 응답 데이터", null)
                        }.start()
                    }
                }
                
                // 코루틴 래퍼
                suspend fun LegacyNetworkClient.fetchDataAsync(): String = 
                    suspendCancellableCoroutine { continuation ->
                        fetchData { data, error ->
                            when {
                                error != null -> continuation.resumeWithException(error)
                                data != null -> continuation.resume(data)
                                else -> continuation.resumeWithException(RuntimeException("Unknown error"))
                            }
                        }
                    }
                
                runBlocking {
                    val client = LegacyNetworkClient()
                    
                    // 이제 동기식처럼 사용 가능!
                    val result = client.fetchDataAsync()
                    result shouldBe "서버 응답 데이터"
                }
            }
        }
    }
    
    given("두 번째 핵심 이점: 취소 지원") {
        `when`("코루틴이 취소될 때") {
            then("콜백 기반 작업도 함께 취소되어 리소스를 절약한다") {
                
                val taskStarted = AtomicBoolean(false)
                val taskCancelled = AtomicBoolean(false)
                
                class CancellableTask {
                    private var cancelled = false
                    
                    fun execute(callback: () -> Unit): () -> Unit {
                        taskStarted.set(true)
                        
                        val thread = Thread {
                            Thread.sleep(500) // 오래 걸리는 작업
                            if (!cancelled) {
                                callback()
                            }
                        }
                        thread.start()
                        
                        // 취소 함수 반환
                        return {
                            cancelled = true
                            thread.interrupt()
                            taskCancelled.set(true)
                        }
                    }
                }
                
                suspend fun CancellableTask.executeAsync(): Unit = 
                    suspendCancellableCoroutine { continuation ->
                        val cancelFn = execute {
                            continuation.resume(Unit)
                        }
                        
                        // 코루틴 취소 시 작업도 취소
                        continuation.invokeOnCancellation { 
                            cancelFn()
                        }
                    }
                
                runBlocking {
                    val task = CancellableTask()
                    
                    val job = launch {
                        task.executeAsync()
                    }
                    
                    delay(100) // 작업 시작 후
                    job.cancelAndJoin()
                    
                    taskStarted.get() shouldBe true      // 작업은 시작됨
                    taskCancelled.get() shouldBe true    // 취소도 됨
                    job.isCancelled shouldBe true
                }
            }
        }
    }
    
    given("세 번째 차이점: suspendCoroutine vs suspendCancellableCoroutine") {
        `when`("일반 suspendCoroutine을 사용하면") {
            then("취소를 지원하지 않는다") {
                
                val taskExecutions = AtomicInteger(0)
                
                class NonCancellableTask {
                    fun execute(callback: () -> Unit) {
                        Thread {
                            Thread.sleep(200)
                            taskExecutions.incrementAndGet()
                            callback()
                        }.start()
                    }
                }
                
                // ❌ 취소를 지원하지 않는 버전
                suspend fun NonCancellableTask.executeSuspend(): Unit = 
                    kotlin.coroutines.suspendCoroutine { continuation ->
                        execute {
                            continuation.resume(Unit)
                        }
                        // 취소 처리 없음!
                    }
                
                runBlocking {
                    val task = NonCancellableTask()
                    
                    val job = launch {
                        task.executeSuspend()
                    }
                    
                    delay(50) // 잠시 후 취소
                    job.cancelAndJoin()
                    
                    delay(200) // 백그라운드 작업 완료 대기
                    
                    // 코루틴은 취소되었지만 백그라운드 작업은 계속 실행됨
                    taskExecutions.get() shouldBe 1
                    job.isCancelled shouldBe true
                }
            }
        }
        
        `when`("suspendCancellableCoroutine을 사용하면") {
            then("취소 처리를 구현할 수 있다") {
                
                val taskStarted = AtomicBoolean(false)
                val taskFinished = AtomicBoolean(false)
                val taskCancelled = AtomicBoolean(false)
                
                class SmartTask {
                    private var cancelled = false
                    
                    fun execute(callback: () -> Unit): () -> Unit {
                        taskStarted.set(true)
                        
                        val thread = Thread {
                            try {
                                Thread.sleep(200)
                                if (!cancelled) {
                                    taskFinished.set(true)
                                    callback()
                                }
                            } catch (e: InterruptedException) {
                                // 취소로 인한 인터럽트
                            }
                        }
                        thread.start()
                        
                        return {
                            cancelled = true
                            thread.interrupt()
                            taskCancelled.set(true)
                        }
                    }
                }
                
                // ✅ 취소를 지원하는 버전
                suspend fun SmartTask.executeAsync(): Unit = 
                    suspendCancellableCoroutine { continuation ->
                        val cancelFn = execute {
                            continuation.resume(Unit)
                        }
                        
                        continuation.invokeOnCancellation {
                            cancelFn()
                        }
                    }
                
                runBlocking {
                    val task = SmartTask()
                    
                    val job = launch {
                        task.executeAsync()
                    }
                    
                    delay(50) // 잠시 후 취소
                    job.cancelAndJoin()
                    
                    delay(100) // 취소 처리 시간
                    
                    taskStarted.get() shouldBe true       // 작업은 시작됨
                    taskFinished.get() shouldBe false     // 완료되지 않음
                    taskCancelled.get() shouldBe true     // 취소 처리됨
                    job.isCancelled shouldBe true
                }
            }
        }
    }

})
