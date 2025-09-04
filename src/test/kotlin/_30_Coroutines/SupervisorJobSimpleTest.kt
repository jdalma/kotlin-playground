package _30_Coroutines

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.collections.shouldContain
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * SupervisorJob의 예외 격리 동작을 보여주는 간단하고 명확한 테스트
 */
class SupervisorJobSimpleTest : BehaviorSpec({

    given("SupervisorJob을 부모로 하는 여러 자식 코루틴이 있을 때") {
        `when`("한 자식 코루틴이 예외를 발생시키면") {
            then("다른 형제 코루틴들은 취소되지 않고 계속 실행되어야 한다 (예외 격리)") {
                runBlocking {
                    val completedCount = AtomicInteger(0)
                    val supervisorJob = SupervisorJob()
                    val scope = CoroutineScope(supervisorJob)

                    // 실패하는 자식 코루틴
                    val failingChild = scope.launch {
                        delay(100)
                        throw RuntimeException("자식 코루틴 실패")
                    }

                    // 성공하는 자식 코루틴들
                    val successChild1 = scope.launch {
                        delay(200)
                        completedCount.incrementAndGet()
                    }

                    val successChild2 = scope.launch {
                        delay(300)
                        completedCount.incrementAndGet()
                    }

                    failingChild.join()
                    
                    // 성공한 자식들은 정상 완료
                    shouldNotThrowAny { successChild1.join() }
                    shouldNotThrowAny { successChild2.join() }

                    // 2개의 성공한 작업이 완료되어야 함
                    completedCount.get() shouldBe 2
                    
                    // SupervisorJob은 여전히 활성 상태
                    supervisorJob.isActive shouldBe true
                    supervisorJob.isCancelled shouldBe false

                    supervisorJob.cancelAndJoin()
                }
            }
        }
    }

    given("일반 Job을 부모로 하는 여러 자식 코루틴이 있을 때") {
        `when`("한 자식 코루틴이 예외를 발생시키면") {
            then("모든 형제 코루틴들이 취소되어야 한다 (예외 격리 없음)") {
                shouldThrow<RuntimeException> {
                    runBlocking {
                        val completedCount = AtomicInteger(0)

                        // 실패하는 자식 코루틴
                        val failingChild = launch {
                            delay(100)
                            throw RuntimeException("자식 코루틴 실패")
                        }

                        // 다른 자식 코루틴들
                        val otherChild1 = launch {
                            try {
                                delay(200)
                                completedCount.incrementAndGet()
                            } catch (e: CancellationException) {
                                // 취소됨
                                throw e
                            }
                        }

                        val otherChild2 = launch {
                            try {
                                delay(300)
                                completedCount.incrementAndGet()
                            } catch (e: CancellationException) {
                                // 취소됨
                                throw e
                            }
                        }

                        // 실패한 자식은 예외를 던짐
                        shouldThrow<RuntimeException> { failingChild.join() }

                        // 다른 자식들은 취소됨
                        shouldThrow<CancellationException> { otherChild1.join() }
                        shouldThrow<CancellationException> { otherChild2.join() }

                        // 완료된 작업은 없어야 함
                        completedCount.get() shouldBe 0

                        // 일반 Job은 취소됨
                        this.isActive shouldBe false
                    }
                }
            }
        }
    }

    given("supervisorScope를 사용할 때") {
        `when`("내부에서 예외를 적절히 처리하면") {
            then("예외가 격리되어 다른 작업들이 계속 진행된다") {
                runTest {
                    val results = mutableListOf<String>()

                    // supervisorScope 내에서 예외를 catch하여 처리
                    supervisorScope {
                        // 실패하는 작업을 async로 시작
                        val failingDeferred = async {
                            delay(100)
                            throw RuntimeException("비동기 작업 실패")
                        }

                        // 성공하는 작업들
                        val success1 = async {
                            delay(200)
                            results.add("작업 1 완료")
                            "작업1 결과"
                        }

                        val success2 = async {
                            delay(300)
                            results.add("작업 2 완료")
                            "작업2 결과"
                        }

                        // 실패한 작업의 예외를 개별적으로 처리

                        try {
                            failingDeferred.await()
                        } catch (e: RuntimeException) {
                            results.add("예외 처리됨: ${e.message}")
                        }

                        // 성공한 작업들은 정상적으로 완료
                        success1.await() shouldBe "작업1 결과"
                        success2.await() shouldBe "작업2 결과"
                    }

                    results.size shouldBe 3
                    results shouldContain "작업 1 완료"
                    results shouldContain "작업 2 완료"
                    results shouldContain "예외 처리됨: 비동기 작업 실패"
                }
            }
        }
    }

    given("coroutineScope(일반 스코프)를 사용할 때") {
        `when`("내부에서 예외가 발생하면") {
            then("전체 스코프가 취소되고 모든 작업이 중단된다") {
                runBlocking {
                    val results = mutableListOf<String>()

                    shouldThrow<RuntimeException> {
                        coroutineScope {
                            // 실패하는 작업
                            async {
                                delay(100)
                                throw RuntimeException("전체를 취소시키는 예외")
                            }

                            // 다른 작업들은 취소됨
                            async {
                                try {
                                    delay(200)
                                    results.add("작업 1 완료")
                                } catch (e: CancellationException) {
                                    results.add("작업 1 취소됨")
                                    throw e
                                }
                            }

                            async {
                                try {
                                    delay(300)
                                    results.add("작업 2 완료")
                                } catch (e: CancellationException) {
                                    results.add("작업 2 취소됨")
                                    throw e
                                }
                            }
                        }
                    }

                    // 완료된 작업은 없고, 취소된 작업만 있음
                    results.size shouldBe 2
                    results shouldContain "작업 1 취소됨"
                    results shouldContain "작업 2 취소됨"
                }
            }
        }
    }

    given("실용적인 사용 사례: 여러 API 호출") {
        `when`("일부 API가 실패해도 다른 API 결과는 필요할 때") {
            then("supervisorScope를 사용하여 부분 실패를 허용할 수 있다") {
                runBlocking {
                    data class ApiResult(val service: String, val data: String?, val error: String?)
                    val results = mutableListOf<ApiResult>()

                    supervisorScope {
                        val apiCalls = listOf(
                            async {
                                delay(50)
                                ApiResult("UserService", "사용자 데이터", null)
                            },
                            async {
                                delay(100)
                                throw RuntimeException("주문 서비스 오류")
                            },
                            async {
                                delay(150)
                                ApiResult("PaymentService", "결제 데이터", null)  
                            },
                            async {
                                delay(200)
                                throw IllegalStateException("재고 서비스 장애")
                            },
                            async {
                                delay(250)
                                ApiResult("NotificationService", "알림 데이터", null)
                            }
                        )

                        // 각 API 호출 결과를 개별 처리
                        val serviceNames = listOf("UserService", "OrderService", "PaymentService", "InventoryService", "NotificationService")
                        
                        apiCalls.forEachIndexed { index, deferred ->
                            try {
                                val result = deferred.await()
                                results.add(result)
                            } catch (e: Exception) {
                                results.add(ApiResult(serviceNames[index], null, e.message))
                            }
                        }
                    }

                    // 모든 서비스 호출이 완료됨 (성공 또는 실패)
                    results.size shouldBe 5

                    val successful = results.filter { it.data != null }
                    val failed = results.filter { it.error != null }

                    successful.size shouldBe 3  // UserService, PaymentService, NotificationService
                    failed.size shouldBe 2      // OrderService, InventoryService

                    // 성공한 서비스 확인
                    val successfulServices = successful.map { it.service }.toSet()
                    successfulServices shouldBe setOf("UserService", "PaymentService", "NotificationService")

                    // 실패한 서비스 확인  
                    val failedServices = failed.map { it.service }.toSet()
                    failedServices shouldBe setOf("OrderService", "InventoryService")
                }
            }
        }

        `when`("일반 coroutineScope를 사용하면") {
            then("하나의 API 실패로 모든 호출이 취소된다") {
                runTest {
                    val results = mutableListOf<String>()

                    shouldThrow<RuntimeException> {
                        runBlocking {
                            listOf(
                                async {
                                    delay(50)
                                    results.add("UserService 완료")
                                },
                                async {
                                    delay(100)  
                                    throw RuntimeException("주문 서비스 실패")
                                },
                                async {
                                    try {
                                        delay(150)
                                        results.add("PaymentService 완료")
                                    } catch (e: CancellationException) {
                                        results.add("PaymentService 취소됨")
                                        throw e
                                    }
                                },
                                async {
                                    try {
                                        delay(200)
                                        results.add("NotificationService 완료")  
                                    } catch (e: CancellationException) {
                                        results.add("NotificationService 취소됨")
                                        throw e
                                    }
                                }
                            )
                        }
                    }

                    // UserService는 실패 전에 완료, 나머지는 취소됨
                    results.size shouldBe 3
                    results shouldContain "UserService 완료"
                    results shouldContain "PaymentService 취소됨"
                    results shouldContain "NotificationService 취소됨"
                }
            }
        }
    }

    given("SupervisorJob vs 일반 Job 직접 비교") {
        `when`("동일한 시나리오로 테스트할 때") {
            then("예외 격리 동작의 차이가 명확히 나타난다") {
                runBlocking {
                    // SupervisorJob 결과
                    var supervisorSuccessCount = 0

                    val supervisorJob = SupervisorJob()
                    val supervisorScope = CoroutineScope(supervisorJob)

                    val supervisorFailJob = supervisorScope.launch {
                        delay(50)
                        throw RuntimeException("SupervisorJob 실패")
                    }

                    val supervisorSuccessJob = supervisorScope.launch {
                        delay(100)
                        supervisorSuccessCount++
                    }

                    shouldNotThrowAny { supervisorFailJob.join() }
                    shouldNotThrowAny { supervisorSuccessJob.join() }

                    supervisorSuccessCount shouldBe 1  // SupervisorJob: 성공 작업 완료
                    supervisorJob.isActive shouldBe true  // SupervisorJob: 여전히 활성

                    supervisorScope.cancel()
                }

                runBlocking {
                    // 일반 Job 결과
                    var regularSuccessCount = 0

                    val regularJob = Job()
                    val regularFailJob = launch(regularJob) {
                        delay(50)
                        throw RuntimeException("일반 Job 실패")
                    }

                    val regularOtherJob = launch(regularJob) {
                        try {
                            delay(100)
                            regularSuccessCount++
                        } catch (e: CancellationException) {
                            throw e
                        }
                    }

                    regularFailJob.join()
                    regularOtherJob.join()

                    regularSuccessCount shouldBe 0     // 일반 Job: 모든 작업 취소
                    regularJob.isCancelled shouldBe true  // 일반 Job: 취소됨
                }
            }
        }
    }
})
