package _30_Coroutines

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * SupervisorJob의 기본적인 예외 격리 동작을 보여주는 테스트
 * 복잡한 코루틴 테스트 프레임워크 대신 runBlocking을 사용하여 안정성 확보
 */
@OptIn(DelicateCoroutinesApi::class)
class SupervisorJobBasicTest : BehaviorSpec({

    given("SupervisorJob을 사용할 때") {
        `when`("한 자식 코루틴이 실패하면") {
            then("다른 자식 코루틴들은 계속 실행되어야 한다 (예외 격리)") {
                runBlocking {
                    val completedCount = AtomicInteger(0)

                    // SupervisorJob을 사용한 스코프 생성
                    val supervisorJob = SupervisorJob()

                    // 실패하는 코루틴
                    val failingJob = launch(supervisorJob) {
                        delay(50)
                        throw RuntimeException("의도적인 실패")
                    }

                    // 성공하는 코루틴들
                    val successJob1 = launch(supervisorJob) {
                        delay(100)
                        completedCount.incrementAndGet()
                    }

                    val successJob2 = launch(supervisorJob) {
                        delay(150)
                        completedCount.incrementAndGet()
                    }

                    failingJob.join()
                    joinAll(successJob1, successJob2)

                    // 2개 작업 완료 확인
                    completedCount.get() shouldBe 2

                    // SupervisorJob은 여전히 활성 상태
                    supervisorJob.isActive shouldBe true
                    supervisorJob.isCancelled shouldBe false

                    supervisorJob.cancelAndJoin() // 정리
                }
            }
        }
    }

    given("일반 Job을 사용할 때") {
        `when`("한 자식 코루틴이 실패하면") {
            then("모든 자식 코루틴들이 취소되어야 한다 (예외 격리 없음)") {
                runBlocking {
                        val completedCount = AtomicInteger(0)
                        val cancelledCount = AtomicInteger(0)

                        // 일반 Job을 사용한 스코프 생성
                        val regularJob = Job()
                        val regularScope = CoroutineScope(regularJob + Dispatchers.Default)

                        // 실패하는 코루틴
                        val failingJob = regularScope.launch {
                            delay(50)
                            throw RuntimeException("의도적인 실패")
                        }

                        // 다른 코루틴들 - 취소될 예정
                        val otherJob1 = regularScope.launch {
                            try {
                                delay(100)
                                completedCount.incrementAndGet()
                            } catch (e: CancellationException) {
                                cancelledCount.incrementAndGet()
                                throw e
                            }
                        }

                        val otherJob2 = regularScope.launch {
                            try {
                                delay(150)
                                completedCount.incrementAndGet()
                            } catch (e: CancellationException) {
                                cancelledCount.incrementAndGet()
                                throw e
                            }
                        }

                        // 실패한 작업 확인
                        shouldThrow<RuntimeException> { failingJob.join() }

                        // 다른 작업들은 취소됨
                        shouldThrow<CancellationException> { otherJob1.join() }
                        shouldThrow<CancellationException> { otherJob2.join() }

                        // 완료된 작업은 없고, 취소된 작업이 있음
                        completedCount.get() shouldBe 0
                        cancelledCount.get() shouldBe 2

                        // 일반 Job은 취소됨
                        regularJob.isActive shouldBe false
                        regularJob.isCancelled shouldBe true
                }
            }
        }
    }

    given("supervisorScope 빌더를 사용할 때") {
        `when`("내부에서 예외를 적절히 처리하면") {
            then("예외가 격리되어 다른 작업들이 계속 진행된다") {
                runBlocking {
                    val results = mutableListOf<String>()
                    
                    supervisorScope {
                        // 여러 비동기 작업 시작
                        val deferreds = listOf(
                            async {
                                delay(50)
                                throw RuntimeException("첫 번째 실패")
                            },
                            async {
                                delay(100)
                                results.add("두 번째 성공")
                                "success-2"
                            },
                            async {
                                delay(150)
                                results.add("세 번째 성공")
                                "success-3"
                            }
                        )
                        
                        // 각각의 결과를 개별적으로 처리
                        try {
                            deferreds[0].await()
                        } catch (e: RuntimeException) {
                            results.add("첫 번째 실패 처리됨")
                        }
                        
                        // 나머지는 정상 처리
                        deferreds[1].await() shouldBe "success-2"
                        deferreds[2].await() shouldBe "success-3"
                    }
                    
                    results.size shouldBe 3
                    results.contains("첫 번째 실패 처리됨") shouldBe true
                    results.contains("두 번째 성공") shouldBe true
                    results.contains("세 번째 성공") shouldBe true
                }
            }
        }
    }

    given("일반 coroutineScope을 사용할 때") {
        `when`("내부에서 예외가 발생하면") {
            then("전체 스코프가 취소되어 모든 작업이 중단된다") {
                runBlocking {
                    val results = mutableListOf<String>()
                    
                    shouldThrow<RuntimeException> {
                        coroutineScope {
                            // 여러 비동기 작업 시작
                            val job1 = async {
                                delay(50)
                                throw RuntimeException("스코프 전체를 실패시킴")
                            }
                            
                            val job2 = async {
                                try {
                                    delay(100)
                                    results.add("두 번째 완료")
                                } catch (e: CancellationException) {
                                    results.add("두 번째 취소됨")
                                    throw e
                                }
                            }
                            
                            val job3 = async {
                                try {
                                    delay(150)
                                    results.add("세 번째 완료")
                                } catch (e: CancellationException) {
                                    results.add("세 번째 취소됨")
                                    throw e
                                }
                            }
                            
                            // 모든 작업 완료 대기 (실제로는 첫 번째에서 예외 발생)
                            awaitAll(job1, job2, job3)
                        }
                    }
                    
                    // 완료된 작업은 없고 취소된 작업만 있음
                    results.size shouldBe 2
                    results.contains("두 번째 취소됨") shouldBe true
                    results.contains("세 번째 취소됨") shouldBe true
                }
            }
        }
    }

    given("실용적인 사용 예제: 여러 서비스 호출") {
        `when`("일부 서비스가 실패해도 다른 서비스 결과는 필요할 때") {
            then("SupervisorJob을 사용하여 부분 실패를 허용할 수 있다") {
                runBlocking {
                    data class ServiceResult(val name: String, val data: String?, val error: String?)
                    val results = mutableListOf<ServiceResult>()
                    
                    supervisorScope {
                        val services = listOf(
                            async {
                                delay(30)
                                ServiceResult("UserService", "사용자 데이터", null)
                            },
                            async {
                                delay(60)
                                throw RuntimeException("주문 서비스 장애")
                            },
                            async {
                                delay(90)
                                ServiceResult("PaymentService", "결제 데이터", null)
                            },
                            async {
                                delay(120)
                                throw IllegalStateException("알림 서비스 오류")
                            }
                        )
                        
                        val serviceNames = listOf("UserService", "OrderService", "PaymentService", "NotificationService")
                        
                        // 각 서비스 결과를 개별 처리
                        services.forEachIndexed { index, deferred ->
                            try {
                                val result = deferred.await()
                                results.add(result)
                            } catch (e: Exception) {
                                results.add(ServiceResult(serviceNames[index], null, e.message))
                            }
                        }
                    }
                    
                    // 모든 서비스 호출이 처리됨 (성공 또는 실패)
                    results.size shouldBe 4
                    
                    val successful = results.filter { it.data != null }
                    val failed = results.filter { it.error != null }
                    
                    // 2개는 성공, 2개는 실패
                    successful.size shouldBe 2
                    failed.size shouldBe 2
                    
                    // 성공한 서비스들
                    successful.map { it.name }.toSet() shouldBe setOf("UserService", "PaymentService")
                    
                    // 실패한 서비스들
                    failed.map { it.name }.toSet() shouldBe setOf("OrderService", "NotificationService")
                }
            }
        }

        `when`("일반 coroutineScope를 사용하면") {
            then("하나의 서비스 실패로 모든 호출이 취소된다") {
                runBlocking {
                    val results = mutableListOf<String>()
                    
                    shouldThrow<RuntimeException> {
                        coroutineScope {
                            launch {
                                delay(30)
                                results.add("UserService 완료")
                            }
                            
                            launch {
                                delay(60)
                                throw RuntimeException("주문 서비스 실패")
                            }
                            
                            launch {
                                try {
                                    delay(90)
                                    results.add("PaymentService 완료")
                                } catch (e: CancellationException) {
                                    results.add("PaymentService 취소됨")
                                    throw e
                                }
                            }
                            
                            launch {
                                try {
                                    delay(120)
                                    results.add("NotificationService 완료")
                                } catch (e: CancellationException) {
                                    results.add("NotificationService 취소됨")
                                    throw e
                                }
                            }
                        }
                    }
                    
                    // UserService는 실패 전에 완료, 나머지는 취소됨
                    results.contains("UserService 완료") shouldBe true
                    results.contains("PaymentService 취소됨") shouldBe true
                    results.contains("NotificationService 취소됨") shouldBe true
                }
            }
        }
    }

    given("SupervisorJob과 일반 Job 차이 비교") {
        `when`("같은 시나리오로 테스트") {
            then("예외 격리의 차이가 명확해야 한다") {
                runBlocking {
                    // SupervisorJob 테스트
                    var supervisorSuccess = 0
                    val supervisorJob = SupervisorJob()
                    val supervisorScope = CoroutineScope(supervisorJob + Dispatchers.Default)
                    
                    val supervisorFail = supervisorScope.launch {
                        delay(30)
                        throw RuntimeException("Supervisor 실패")
                    }
                    
                    val supervisorOk = supervisorScope.launch {
                        delay(60)
                        supervisorSuccess++
                    }
                    
                    shouldThrow<RuntimeException> { supervisorFail.join() }
                    supervisorOk.join() // 예외 없음
                    
                    // 일반 Job 테스트  
                    var regularSuccess = 0
                    val regularJob = Job()
                    val regularScope = CoroutineScope(regularJob + Dispatchers.Default)
                    
                    val regularFail = regularScope.launch {
                        delay(30)
                        throw RuntimeException("Regular 실패")
                    }
                    
                    val regularOther = regularScope.launch {
                        try {
                            delay(60)
                            regularSuccess++
                        } catch (e: CancellationException) {
                            throw e
                        }
                    }
                    
                    shouldThrow<RuntimeException> { regularFail.join() }
                    shouldThrow<CancellationException> { regularOther.join() }
                    
                    // 결과 비교
                    supervisorSuccess shouldBe 1  // SupervisorJob: 성공 작업 완료
                    regularSuccess shouldBe 0     // 일반 Job: 모든 작업 취소
                    
                    supervisorJob.isActive shouldBe true   // 여전히 활성
                    regularJob.isCancelled shouldBe true   // 취소됨
                    
                    supervisorJob.cancel() // 정리
                }
            }
        }
    }
})
