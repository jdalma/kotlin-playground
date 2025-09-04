package _30_Coroutines

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.*
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

/**
 * CancellationException의 특별한 성격과 올바른 처리 방법을 보여주는 테스트
 * - 협력적 취소 메커니즘
 * - 올바른 처리 vs 잘못된 처리
 * - Job 상태와 구조화된 동시성에 미치는 영향
 */
class CancellationExceptionTest : BehaviorSpec({

    given("CancellationException을 잘못 처리할 때") {
        `when`("예외를 catch로 잡고 무시해도") {
            then("Job이 취소된다.") {
                runBlocking {
                    var executionCompleted = false
                    var cancellationCaught = false
                    
                    val job = launch {
                        try {
                            delay(1000)
                            executionCompleted = true
                        } catch (e: CancellationException) {
                            cancellationCaught = true
                            // ❌ 잘못된 처리 - 예외를 다시 던지지 않음
                        }
                    }
                    
                    delay(100)
                    job.cancel() // 취소 요청
                    job.join()
                    
                    // 예외는 잡혔지만 Job은 정상 완료됨
                    cancellationCaught shouldBe true
                    executionCompleted shouldBe false
                    job.isCancelled shouldBe true
                    job.isCompleted shouldBe true
                }
            }
        }

        `when`("부모 Job이 취소되면") {
            then("자식들도 모두 취소된다.") {
                runBlocking {
                    var child1Completed = false
                    var child2Cancelled = false
                    var child3Cancelled = false
                    var parentJobCompleted = false
                    
                    val parentJob = launch {
                        // 자식 1: CancellationException을 무시
                        launch {
                            try {
                                delay(500)
                                child1Completed = true
                            } catch (e: CancellationException) {
                                // 예외를 다시 던지지 않음
                            }
                        }
                        
                        // 자식 2: 정상적인 취소 처리
                        launch {
                            try {
                                delay(500)
                            } catch (e: CancellationException) {
                                child2Cancelled = true
                                throw e
                            }
                        }
                        
                        // 자식 3: 정상적인 취소 처리
                        launch {
                            try {
                                delay(500)
                            } catch (e: CancellationException) {
                                child3Cancelled = true
                                throw e
                            }
                        }
                        delay(300)
                        parentJobCompleted = true
                    }
                    
                    delay(100)
                    parentJob.cancel()
                    parentJob.join()
                    
                    // 결과 검증
                    child1Completed shouldBe false
                    child2Cancelled shouldBe true
                    child3Cancelled shouldBe true
                    parentJobCompleted shouldBe false
                    
                    parentJob.isCancelled shouldBe true
                }
            }
        }
    }

    given("CancellationException을 올바르게 처리할 때") {
        `when`("정리 작업 후 예외를 다시 던지면") {
            then("Job이 올바르게 취소되어야 한다") {
                runBlocking {
                    var cleanupExecuted = false
                    var cancellationHandled = false
                    
                    val job = launch {
                        try {
                            delay(1000)
                        } catch (e: CancellationException) {
                            cancellationHandled = true
                            // ✅ 올바른 처리 - 정리 작업 후 재던지기
                            cleanupExecuted = true
                            throw e // 반드시 재던지기!
                        }
                    }
                    
                    delay(100)
                    job.cancel()
                    job.join()
                    
                    // 올바른 취소 처리 확인
                    cancellationHandled shouldBe true
                    cleanupExecuted shouldBe true
                    job.isCancelled shouldBe true    // 올바르게 취소됨
                    job.isCompleted shouldBe true    // 취소도 완료의 한 형태
                }
            }
        }

        `when`("리소스 정리가 필요한 상황에서") {
            then("정리 작업을 수행한 후 예외를 재던져야 한다") {
                runBlocking {
                    val resourcesCreated = AtomicInteger(0)
                    val resourcesCleaned = AtomicInteger(0)
                    
                    suspend fun createResource(): String {
                        resourcesCreated.incrementAndGet()
                        return "Resource-${resourcesCreated.get()}"
                    }
                    
                    fun cleanupResource(resource: String) {
                        resourcesCleaned.incrementAndGet()
                        println("정리: $resource")
                    }
                    
                    val job = launch {
                        val resource1 = createResource()
                        val resource2 = createResource()
                        
                        try {
                            delay(1000) // 장기 실행 작업
                        } catch (e: CancellationException) {
                            cleanupResource(resource1)
                            cleanupResource(resource2)
                            throw e // 정리 후 재던지기
                        }
                    }
                    
                    delay(100)
                    job.cancel()
                    job.join()
                    
                    // 리소스 정리 확인
                    resourcesCreated.get() shouldBe 2
                    resourcesCleaned.get() shouldBe 2 // 모든 리소스가 정리됨
                    job.isCancelled shouldBe true
                }
            }
        }
    }

    given("구조화된 동시성과 CancellationException") {
        `when`("coroutineScope에서 한 자식이 취소를 무시하면") {
            then("다른 자식들은 취소되지만 전체 스코프는 영향받지 않는다") {
                runBlocking {
                    var scopeCompleted = false
                    var child1IgnoredCancel = false
                    var child2Cancelled = false
                    
                    try {
                        coroutineScope {
                            // 자식 1: 취소 무시
                            val child1Job = launch {
                                try {
                                    delay(300)
                                } catch (e: CancellationException) {
                                    child1IgnoredCancel = true
                                    // 재던지지 않음
                                }
                            }
                            
                            // 자식 2: 정상 취소 처리
                            launch {
                                try {
                                    delay(300)
                                } catch (e: CancellationException) {
                                    child2Cancelled = true
                                    throw e
                                }
                            }
                            delay(100)
                            child1Job.cancelAndJoin()
                        }
                        scopeCompleted = true
                    } catch (e: CancellationException) {
                        println("coroutineScope 취소됨")
                    }
                    
                    scopeCompleted shouldBe true
                    child1IgnoredCancel shouldBe true
                    child2Cancelled shouldBe false
                }
            }
        }

        `when`("부모 Job이 취소되면") {
            then("모든 자식이 CancellationException을 받아야 한다") {
                runBlocking {
                    val cancelledChildren = AtomicInteger(0)
                    val completedChildren = AtomicInteger(0)
                    
                    val parentJob = launch {
                        repeat(5) { i ->
                            launch {
                                try {
                                    delay(1000)
                                    completedChildren.incrementAndGet()
                                } catch (e: CancellationException) {
                                    cancelledChildren.incrementAndGet()
                                    throw e
                                }
                            }
                        }
                    }
                    
                    delay(100)
                    parentJob.cancel() // 부모 취소
                    parentJob.join()
                    
                    // 모든 자식이 취소되어야 함
                    cancelledChildren.get() shouldBe 5
                    completedChildren.get() shouldBe 0
                    parentJob.isCancelled shouldBe true
                }
            }
        }
    }

    given("코루틴 빌더 밖에서 예외를 잡는 경우") {
        `when`("launch 밖에서 try-catch로 감싸면") {
            then("CancellationException은 잡히지 않는다") {
                runBlocking {
                    var exceptionCaught = false
                    var jobCancelled = false
                    
                    try {
                        val job = launch {
                            delay(1000)
                        }
                        
                        delay(100)
                        job.cancel()
                        job.join()
                        jobCancelled = job.isCancelled
                        
                    } catch (e: CancellationException) {
                        exceptionCaught = true
                    }
                    
                    // launch는 fire-and-forget이므로 외부 try-catch에서 잡히지 않음
                    exceptionCaught shouldBe false
                    jobCancelled shouldBe true
                }
            }
        }

        `when`("async 밖에서 try-catch로 감싸고 await을 호출하면") {
            then("CancellationException이 await에서 던져진다") {
                runBlocking {
                    var exceptionCaught = false
                    var exceptionMessage = ""
                    
                    try {
                        val deferred = async {
                            delay(1000)
                            "결과"
                        }
                        
                        delay(100)
                        deferred.cancel()
                        val result = deferred.await() // 여기서 CancellationException 발생

                    } catch (e: CancellationException) {
                        exceptionCaught = true
                        exceptionMessage = e.message ?: "CancellationException"
                    }
                    
                    // await()에서 CancellationException이 던져져서 외부에서 잡힘
                    exceptionCaught shouldBe true
                    exceptionMessage.isNotEmpty() shouldBe true
                }
            }
        }

        `when`("runBlocking 밖에서 try-catch로 감싸면") {
            then("runBlocking 내부의 uncaught exception이 전파된다") {
                var exceptionCaught = false
                var exceptionMessage = ""
                var taskCompleted = false
                
                try {
                    runBlocking {
                        launch {
                            delay(100)
                            throw RuntimeException("런타임 예외")
                        }
                        
                        launch {
                            delay(200)
                            taskCompleted = true
                        }
                        
                        delay(1000) // 모든 자식 완료까지 대기
                    }
                } catch (e: RuntimeException) {
                    exceptionCaught = true
                    exceptionMessage = e.message ?: ""
                }
                
                exceptionCaught shouldBe true
                exceptionMessage shouldBe "런타임 예외"
                taskCompleted shouldBe false
            }
        }

        `when`("coroutineScope 밖에서 try-catch로 감싸면") {
            then("내부 예외가 호출자로 전파된다") {
                runBlocking {
                    var exceptionCaught = false
                    var exceptionType = ""
                    
                    try {
                        coroutineScope {
                            launch {
                                delay(100)
                                throw IllegalStateException("상태 오류")
                            }
                            
                            launch {
                                delay(200)
                                println("두 번째 작업 (취소됨)")
                            }
                        }
                    } catch (e: IllegalStateException) {
                        exceptionCaught = true
                        exceptionType = "IllegalStateException"
                    } catch (e: CancellationException) {
                        exceptionCaught = true
                        exceptionType = "CancellationException"
                    }
                    
                    exceptionCaught shouldBe true
                    exceptionType shouldBe "IllegalStateException"
                }
            }
        }

        `when`("supervisorScope 밖에서 try-catch로 감싸면") {
            then("처리되지 않은 예외만 전파된다") {
                runBlocking {
                    var exceptionCaught = false
                    var childrenCompleted = 0
                    var childrenException = 0
                    
                    try {
                        supervisorScope {
                            // 자식 1: 예외 발생하지만 내부에서 처리
                            launch {
                                try {
                                    delay(100)
                                    throw RuntimeException("내부 처리될 예외")
                                } catch (e: RuntimeException) {
                                    childrenException++
                                }
                            }
                            
                            // 자식 2: 정상 완료
                            launch {
                                delay(150)
                                childrenCompleted++
                            }
                            
                            // 자식 3: 처리되지 않은 예외 (supervisorScope로 전파)
                            launch {
                                delay(200)
                                throw IllegalArgumentException("처리되지 않은 예외")
                            }
                        }
                    } catch (e: Exception) {
                        exceptionCaught = true
                    }
                    
                    delay(300) // 모든 작업 완료 대기
                    
                    exceptionCaught shouldBe false
                    childrenCompleted shouldBe 1
                    childrenException shouldBe 1
                }
            }
        }

        `when`("Job.join()을 try-catch로 감싸면") {
            then("CancellationException은 join에서 던져지지 않는다") {
                runBlocking {
                    var exceptionCaught = false
                    
                    val job = launch {
                        delay(1000)
                        println("작업 완료")
                    }
                    
                    job.cancel()
                    
                    try {
                        job.join() // join()은 CancellationException을 던지지 않음
                    } catch (e: CancellationException) {
                        exceptionCaught = true
                    }
                    
                    exceptionCaught shouldBe false // join()은 예외를 던지지 않음
                    job.isCancelled shouldBe true
                }
            }
        }

        `when`("Deferred.await()을 try-catch로 감싸면") {
            then("취소된 경우 CancellationException이 던져진다") {
                runBlocking {
                    var exceptionCaught = false
                    var resultObtained = false
                    
                    val deferred = async {
                        delay(1000)
                        "최종 결과"
                    }
                    
                    delay(100)
                    deferred.cancel()
                    
                    try {
                        val result = deferred.await() // await()은 CancellationException을 던짐
                        resultObtained = true
                    } catch (e: CancellationException) {
                        exceptionCaught = true
                    }
                    
                    exceptionCaught shouldBe true
                    resultObtained shouldBe false
                    deferred.isCancelled shouldBe true
                }
            }
        }
    }

    given("실제 사용 시나리오") {
        `when`("파일 다운로드 중 취소가 발생하면") {
            then("임시 파일을 정리하고 취소를 전파해야 한다") {
                runBlocking {
                    val tempFilesCreated = AtomicInteger(0)
                    val tempFilesDeleted = AtomicInteger(0)
                    
                    suspend fun downloadFile(filename: String): String {
                        val tempFile = "temp_$filename"
                        tempFilesCreated.incrementAndGet()

                        try {
                            // 다운로드 시뮬레이션
                            repeat(10) {
                                delay(50)
                                println("다운로드 진행: ${it * 10}%")
                            }
                            return "downloaded_$filename"
                        } catch (e: CancellationException) {
                            println("다운로드 취소 - 임시 파일 삭제: $tempFile")
                            tempFilesDeleted.incrementAndGet()
                            throw e // 반드시 재던지기
                        }
                    }
                    
                    val jobs = listOf(
                        launch { downloadFile("file1.zip") },
                        launch { downloadFile("file2.zip") },
                        launch { downloadFile("file3.zip") }
                    )
                    
                    delay(150) // 일부 진행 후 취소
                    jobs.forEach { it.cancel() }
                    jobs.joinAll()
                    
                    // 정리 검증
                    tempFilesCreated.get() shouldBe 3  // 3개 임시 파일 생성
                    tempFilesDeleted.get() shouldBe 3  // 3개 모두 정리됨
                    jobs.all { it.isCancelled } shouldBe true

                }
            }
        }
    }
})
