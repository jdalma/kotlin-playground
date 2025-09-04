package _30_Coroutines

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.collections.shouldContain
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import java.util.concurrent.atomic.AtomicInteger

class SupervisorJobExceptionIsolationTest : BehaviorSpec({

    given("SupervisorJob을 사용한 코루틴 스코프가 있을 때") {
        `when`("자식 코루틴 중 하나가 예외를 발생시키면") {
            then("다른 자식 코루틴들은 영향받지 않고 계속 실행되어야 한다 (예외 격리)") {
                runTest {
                    val completedJobs = AtomicInteger(0)
                    
                    supervisorScope {
                        // 예외를 발생시키는 코루틴 - try-catch로 처리
                        val failingJob = launch {
                            try {
                                delay(100)
                                throw RuntimeException("자식 코루틴에서 예외 발생")
                            } catch (e: RuntimeException) {
                                // 예외를 처리해서 supervisorScope를 벗어나지 않게 함
                                println("예외 처리됨: ${e.message}")
                            }
                        }

                        // 정상 실행되는 코루틴들
                        val job2 = launch {
                            delay(200)
                            completedJobs.incrementAndGet()
                        }

                        val job3 = launch {
                            delay(300)
                            completedJobs.incrementAndGet()
                        }

                        // 모든 작업이 정상 완료됨
                        shouldNotThrowAny { failingJob.join() }
                        shouldNotThrowAny { job2.join() }
                        shouldNotThrowAny { job3.join() }
                    }

                    // 2개의 작업이 완료되어야 함
                    completedJobs.get() shouldBe 2
                }
            }

            then("supervisorScope에서 async로 시작된 작업들도 격리되어야 한다") {
                runTest {
                    val results = mutableListOf<String>()
                    
                    supervisorScope {
                        val deferred1 = async {
                            delay(100)
                            throw RuntimeException("첫 번째 작업 실패")
                        }

                        val deferred2 = async {
                            delay(150)
                            results.add("두 번째 작업 완료")
                            "success-2"
                        }

                        val deferred3 = async {
                            delay(200)
                            results.add("세 번째 작업 완료")
                            "success-3"
                        }

                        // 첫 번째 작업은 실패하지만 await()에서 예외 처리
                        try {
                            deferred1.await()
                        } catch (e: RuntimeException) {
                            println("첫 번째 작업 실패 처리: ${e.message}")
                        }
                        
                        // 나머지는 성공
                        deferred2.await() shouldBe "success-2"
                        deferred3.await() shouldBe "success-3"
                    }

                    results.size shouldBe 2
                    results shouldContain "두 번째 작업 완료"
                    results shouldContain "세 번째 작업 완료"
                }
            }
        }

        `when`("여러 예외가 동시에 발생할 때") {
            then("각각 독립적으로 처리되고 정상 작업들은 계속 실행되어야 한다") {
                runTest {
                    val completedTasks = AtomicInteger(0)

                    supervisorScope {
                        val jobs = (0..4).map { index ->
                            launch {
                                delay((index + 1) * 50L)
                                when (index) {
                                    1 -> throw RuntimeException("Task 1 failed")
                                    3 -> throw IllegalStateException("Task 3 failed")
                                    else -> completedTasks.incrementAndGet()
                                }
                            }
                        }

                        // 각 작업을 개별적으로 처리
                        jobs.forEach { job ->
                            try {
                                job.join()
                            } catch (e: Exception) {
                                println("작업 실패 처리: ${e.message}")
                            }
                        }
                    }

                    // 5개 중 2개가 실패했으므로 3개가 완료되어야 함
                    completedTasks.get() shouldBe 3
                }
            }
        }
    }

    given("일반 coroutineScope(SupervisorJob이 아닌)를 사용할 때") {
        `when`("자식 코루틴 중 하나가 예외를 발생시키면") {
            then("전체 스코프가 취소되고 모든 형제 코루틴들이 취소되어야 한다 (예외 격리 없음)") {
                runTest {
                    val results = mutableListOf<String>()

                    shouldThrow<RuntimeException> {
                        coroutineScope {
                            launch {
                                delay(100)
                                throw RuntimeException("스코프를 실패시키는 예외")
                            }
                            
                            launch {
                                try {
                                    delay(200)
                                    results.add("이 작업은 완료되지 않음")
                                } catch (e: CancellationException) {
                                    results.add("두 번째 작업이 취소됨")
                                    throw e
                                }
                            }
                            
                            launch {
                                try {
                                    delay(300)
                                    results.add("이 작업도 완료되지 않음")
                                } catch (e: CancellationException) {
                                    results.add("세 번째 작업이 취소됨")
                                    throw e
                                }
                            }
                        }
                    }

                    // 정상 완료된 작업은 없고, 취소된 작업만 있어야 함
                    results.size shouldBe 2
                    results shouldContain "두 번째 작업이 취소됨"
                    results shouldContain "세 번째 작업이 취소됨"
                }
            }

            then("async로 시작된 작업들도 모두 취소되어야 한다") {
                runTest {
                    val results = mutableListOf<String>()

                    shouldThrow<RuntimeException> {
                        coroutineScope {
                            val deferred1 = async {
                                delay(100)
                                throw RuntimeException("전체를 실패시키는 작업")
                            }

                            val deferred2 = async {
                                try {
                                    delay(200)
                                    results.add("정상 완료")
                                    "success"
                                } catch (e: CancellationException) {
                                    results.add("async 작업 취소됨")
                                    throw e
                                }
                            }

                            deferred1.await()
                            deferred2.await()
                        }
                    }

                    results.size shouldBe 1
                    results shouldContain "async 작업 취소됨"
                }
            }
        }
    }

    given("SupervisorJob과 일반 Job의 차이를 직접 비교할 때") {
        `when`("동일한 조건에서 예외 처리를 테스트하면") {
            then("SupervisorJob은 예외를 격리하고 일반 Job은 전파해야 한다") {
                runTest {
                    // SupervisorJob 테스트
                    val supervisorResults = mutableListOf<String>()
                    
                    supervisorScope {
                        val failingJob = launch {
                            delay(50)
                            throw RuntimeException("SupervisorJob 내 예외")
                        }
                        
                        val successJob = launch {
                            delay(100)
                            supervisorResults.add("SupervisorJob에서 완료됨")
                        }

                        // 예외는 개별적으로 처리
                        try {
                            failingJob.join()
                        } catch (e: RuntimeException) {
                            println("SupervisorJob 예외 처리: ${e.message}")
                        }

                        successJob.join()
                    }

                    // 일반 coroutineScope 테스트
                    val regularResults = mutableListOf<String>()

                    shouldThrow<RuntimeException> {
                        coroutineScope {
                            launch {
                                delay(50)
                                throw RuntimeException("일반 Job 내 예외")
                            }
                            
                            launch {
                                try {
                                    delay(100)
                                    regularResults.add("일반 Job에서 완료됨")
                                } catch (e: CancellationException) {
                                    regularResults.add("일반 Job에서 취소됨")
                                    throw e
                                }
                            }
                        }
                    }

                    // 결과 비교
                    supervisorResults.size shouldBe 1
                    supervisorResults[0] shouldBe "SupervisorJob에서 완료됨"

                    regularResults.size shouldBe 1
                    regularResults[0] shouldBe "일반 Job에서 취소됨"
                }
            }
        }
    }

    given("중첩된 스코프 구조가 있을 때") {
        `when`("내부 supervisorScope에서 예외가 발생하면") {
            then("외부 스코프는 영향받지 않아야 한다") {
                runTest {
                    val outerResults = mutableListOf<String>()
                    val innerResults = mutableListOf<String>()

                    supervisorScope { // 외부 SupervisorScope
                        launch {
                            delay(100)
                            outerResults.add("외부 작업 1 완료")
                        }

                        launch {
                            supervisorScope { // 내부 SupervisorScope
                                val innerFailingJob = launch {
                                    delay(50)
                                    throw RuntimeException("내부에서 예외 발생")
                                }
                                
                                val innerSuccessJob = launch {
                                    delay(100)
                                    innerResults.add("내부 작업 완료")
                                }

                                // 내부 예외 처리
                                try {
                                    innerFailingJob.join()
                                } catch (e: RuntimeException) {
                                    println("내부 예외 처리: ${e.message}")
                                }

                                innerSuccessJob.join()
                            }
                        }

                        launch {
                            delay(150)
                            outerResults.add("외부 작업 2 완료")
                        }
                    }

                    // 모든 작업이 완료되어야 함
                    outerResults.size shouldBe 2
                    innerResults.size shouldBe 1
                    outerResults shouldContain "외부 작업 1 완료"
                    outerResults shouldContain "외부 작업 2 완료"
                    innerResults shouldContain "내부 작업 완료"
                }
            }
        }

        `when`("내부 coroutineScope에서 예외가 발생하면") {
            then("해당 스코프만 실패하고 외부 supervisorScope는 계속되어야 한다") {
                runTest {
                    val outerResults = mutableListOf<String>()

                    supervisorScope { // 외부 SupervisorScope
                        launch {
                            delay(100)
                            outerResults.add("외부 작업 1 완료")
                        }

                        launch {
                            try {
                                coroutineScope { // 내부 일반 스코프
                                    launch {
                                        delay(50)
                                        throw RuntimeException("내부 스코프 실패")
                                    }
                                    
                                    launch {
                                        delay(100)
                                        // 이 작업은 취소됨
                                    }
                                }
                            } catch (e: RuntimeException) {
                                outerResults.add("내부 스코프 예외 처리됨")
                            }
                        }

                        launch {
                            delay(150)
                            outerResults.add("외부 작업 2 완료")
                        }
                    }

                    // 외부 작업들과 예외 처리가 모두 완료되어야 함
                    outerResults.size shouldBe 3
                    outerResults shouldContain "외부 작업 1 완료"
                    outerResults shouldContain "내부 스코프 예외 처리됨"
                    outerResults shouldContain "외부 작업 2 완료"
                }
            }
        }
    }

    given("실제 사용 사례를 모델링할 때") {
        `when`("병렬로 여러 API 호출을 하고 일부가 실패해도 나머지는 계속되어야 할 때") {
            then("supervisorScope를 사용하여 안정적인 병렬 처리가 가능해야 한다") {
                runTest {
                    data class ApiResult(val id: Int, val data: String?, val error: String?)
                    
                    val results = mutableListOf<ApiResult>()

                    supervisorScope {
                        // 여러 API 호출 시뮬레이션
                        val apiCalls = (1..5).map { id ->
                            async {
                                delay(id * 30L) // 다양한 응답 시간
                                when (id) {
                                    2 -> throw RuntimeException("API $id 호출 실패")
                                    4 -> throw IllegalStateException("API $id 서버 오류")
                                    else -> ApiResult(id, "API $id 데이터", null)
                                }
                            }
                        }

                        // 각 API 호출 결과를 개별적으로 처리
                        apiCalls.forEachIndexed { index, deferred ->
                            try {
                                val result = deferred.await()
                                results.add(result)
                            } catch (e: Exception) {
                                results.add(ApiResult(index + 1, null, e.message))
                            }
                        }
                    }

                    // 모든 API 호출이 완료되고 성공/실패 정보가 수집되어야 함
                    results.size shouldBe 5
                    
                    val successful = results.filter { it.data != null }
                    val failed = results.filter { it.error != null }
                    
                    successful.size shouldBe 3  // ID 1, 3, 5가 성공
                    failed.size shouldBe 2      // ID 2, 4가 실패
                    
                    // 성공한 결과 검증
                    successful.map { it.id }.toSet() shouldBe setOf(1, 3, 5)
                    
                    // 실패한 결과 검증
                    failed.map { it.id }.toSet() shouldBe setOf(2, 4)
                    failed.all { it.error != null } shouldBe true
                }
            }
        }
    }

    given("SupervisorJob의 핵심 동작을 명확히 보여주는 시나리오가 있을 때") {
        `when`("직접적인 SupervisorJob 사용") {
            then("자식 Job의 실패가 형제 Job에 영향을 주지 않아야 한다") {
                runTest {
                    val completedJobs = AtomicInteger(0)
                    val supervisorJob = SupervisorJob()
                    
                    val job1 = CoroutineScope(supervisorJob + StandardTestDispatcher(testScheduler)).launch {
                        delay(50)
                        throw RuntimeException("Job 1 실패")
                    }

                    val job2 = CoroutineScope(supervisorJob + StandardTestDispatcher(testScheduler)).launch {
                        delay(100)
                        completedJobs.incrementAndGet()
                    }

                    val job3 = CoroutineScope(supervisorJob + StandardTestDispatcher(testScheduler)).launch {
                        delay(150)
                        completedJobs.incrementAndGet()
                    }

                    // job1은 실패하지만 다른 작업들은 계속 진행
                    shouldThrow<RuntimeException> { job1.join() }
                    shouldNotThrowAny { job2.join() }
                    shouldNotThrowAny { job3.join() }

                    completedJobs.get() shouldBe 2
                    supervisorJob.isActive shouldBe true
                }
            }

            then("일반 Job과 비교했을 때 예외 격리 차이가 명확해야 한다") {
                runTest {
                    // 일반 Job 테스트
                    val regularCompletedJobs = AtomicInteger(0)
                    val regularJob = Job()
                    
                    val regularJob1 = CoroutineScope(regularJob + StandardTestDispatcher(testScheduler)).launch {
                        delay(50)
                        throw RuntimeException("Regular Job 1 실패")
                    }

                    val regularJob2 = CoroutineScope(regularJob + StandardTestDispatcher(testScheduler)).launch {
                        try {
                            delay(100)
                            regularCompletedJobs.incrementAndGet()
                        } catch (e: CancellationException) {
                            // 취소됨
                            throw e
                        }
                    }

                    // 첫 번째 작업의 실패로 인해 전체가 취소됨
                    shouldThrow<RuntimeException> { regularJob1.join() }
                    shouldThrow<CancellationException> { regularJob2.join() }
                    
                    regularCompletedJobs.get() shouldBe 0
                    regularJob.isCancelled shouldBe true

                    // SupervisorJob 테스트
                    val supervisorCompletedJobs = AtomicInteger(0)
                    val supervisorJob = SupervisorJob()
                    
                    val supervisorJob1 = CoroutineScope(supervisorJob + StandardTestDispatcher(testScheduler)).launch {
                        delay(50)
                        throw RuntimeException("Supervisor Job 1 실패")
                    }

                    val supervisorJob2 = CoroutineScope(supervisorJob + StandardTestDispatcher(testScheduler)).launch {
                        delay(100)
                        supervisorCompletedJobs.incrementAndGet()
                    }

                    // SupervisorJob에서는 격리됨
                    shouldThrow<RuntimeException> { supervisorJob1.join() }
                    shouldNotThrowAny { supervisorJob2.join() }
                    
                    supervisorCompletedJobs.get() shouldBe 1
                    supervisorJob.isActive shouldBe true
                }
            }
        }
    }
})