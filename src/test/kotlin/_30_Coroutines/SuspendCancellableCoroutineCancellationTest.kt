package _30_Coroutines

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.assertions.throwables.shouldThrow
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.schedule

/**
 * suspendCancellableCoroutine의 취소 기능을 명확하게 보여주는 테스트
 * 
 * 핵심 개념:
 * 1. invokeOnCancellation - 코루틴 취소 시 정리 작업 수행
 * 2. isActive 체크 - 취소 전 상태 확인
 * 3. 리소스 해제 - 취소 시 자동으로 리소스 정리
 */
class SuspendCancellableCoroutineCancellationTest : BehaviorSpec({

    given("취소 가능한 타이머 작업") {
        `when`("일반 타이머를 코루틴으로 래핑할 때") {
            then("코루틴 취소 시 타이머도 함께 취소된다") {
                val timerExecuted = AtomicBoolean(false)
                val timerCancelled = AtomicBoolean(false)
                suspend fun delayWithTimer(millis: Long): Unit = suspendCancellableCoroutine { cont ->
                    val timer = Timer()
                    val task = timer.schedule(millis) {
                        timerExecuted.set(true)
                        cont.resume(Unit)
                        timer.cancel()
                    }
                    
                    // 🔑 핵심: 코루틴 취소 시 타이머도 취소
                    cont.invokeOnCancellation {
                        println("코루틴 취소됨 - 타이머 정리")
                        timerCancelled.set(true)
                        task.cancel()
                        timer.cancel()
                    }
                }
                
                val job = launch {
                    delayWithTimer(1000) // 1초 대기
                }
                
                delay(100) // 잠시 대기
                job.cancel() // 취소!
                job.join()
                
                timerExecuted.get() shouldBe false // 타이머 실행 안됨
                timerCancelled.get() shouldBe true // 취소 핸들러는 실행됨
            }
        }

        `when`("여러 타이머를 동시에 실행할 때") {
            then("특정 타이머만 선택적으로 취소할 수 있다") {
                val completedTasks = mutableListOf<String>()
                val cancelledTasks = mutableListOf<String>()
                
                suspend fun timedTask(name: String, delayMs: Long): String = 
                    suspendCancellableCoroutine { cont ->
                        val timer = Timer()
                        timer.schedule(delayMs) {
                            completedTasks.add(name)
                            cont.resume("$name 완료")
                            timer.cancel()
                        }
                        
                        cont.invokeOnCancellation {
                            cancelledTasks.add(name)
                            timer.cancel()
                            println("$name 취소됨")
                        }
                    }
                
                coroutineScope {
                    val job1 = launch { timedTask("작업1", 200) }
                    val job2 = launch { timedTask("작업2", 300) }
                    val job3 = launch { timedTask("작업3", 400) }
                    
                    delay(100)
                    job2.cancel() // 작업2만 취소
                    
                    delay(350) // 작업1은 완료, 작업3은 진행중
                }
                
                completedTasks shouldBe listOf("작업1", "작업3")
                cancelledTasks shouldBe listOf("작업2")
            }
        }
    }

    given("데이터베이스 연결 관리") {
        `when`("DB 연결을 코루틴으로 관리할 때") {
            then("취소 시 연결이 자동으로 해제된다") {
                // 가상의 DB 연결
                class DatabaseConnection(val id: String) {
                    var isOpen = true
                    
                    fun query(sql: String): String {
                        if (!isOpen) throw IllegalStateException("연결이 닫혔습니다")
                        Thread.sleep(100) // 쿼리 실행 시뮬레이션
                        return "결과: $sql"
                    }
                    
                    fun close() {
                        isOpen = false
                        println("DB 연결 종료: $id")
                    }
                }
                
                suspend fun withDatabase(block: suspend (DatabaseConnection) -> Unit) = 
                    suspendCancellableCoroutine<Unit> { cont ->
                        val connection = DatabaseConnection("DB-${System.currentTimeMillis()}")
                        
                        // 🔑 취소 시 DB 연결 자동 해제
                        cont.invokeOnCancellation {
                            if (connection.isOpen) {
                                connection.close()
                            }
                        }
                        
                        // 비동기로 블록 실행
                        GlobalScope.launch {
                            try {
                                block(connection)
                                cont.resume(Unit)
                            } catch (e: Exception) {
                                cont.resumeWithException(e)
                            } finally {
                                if (connection.isOpen) {
                                    connection.close()
                                }
                            }
                        }
                    }
                
                var dbConnection: DatabaseConnection? = null
                
                val job = launch {
                    withDatabase { db ->
                        dbConnection = db
                        db.query("SELECT * FROM users")
                        delay(1000) // 긴 작업 시뮬레이션
                        db.query("UPDATE users SET active = true")
                    }
                }
                
                delay(50) // 첫 쿼리는 실행되도록
                job.cancel() // 취소!
                job.join()
                
                delay(10) // 정리 시간
                dbConnection?.isOpen shouldBe false // DB 연결이 자동으로 해제됨
            }
        }
    }

    given("파일 다운로드 시나리오") {
        `when`("대용량 파일을 다운로드할 때") {
            then("취소 시 다운로드가 즉시 중단되고 리소스가 정리된다") {
                class FileDownloader {
                    private var downloadThread: Thread? = null
                    @Volatile private var cancelled = false
                    
                    fun startDownload(
                        url: String,
                        onProgress: (Int) -> Unit,
                        onComplete: (ByteArray) -> Unit,
                        onError: (Exception) -> Unit
                    ): () -> Unit {
                        cancelled = false
                        downloadThread = Thread {
                            try {
                                val totalSize = 1000 // 가상의 파일 크기
                                val buffer = ByteArray(totalSize)
                                
                                // 다운로드 시뮬레이션 (10% 단위)
                                for (progress in 10..100 step 10) {
                                    if (cancelled) {
                                        println("다운로드 취소됨: $progress%")
                                        return@Thread
                                    }
                                    Thread.sleep(100) // 다운로드 시간
                                    onProgress(progress)
                                }
                                
                                if (!cancelled) {
                                    onComplete(buffer)
                                }
                            } catch (e: InterruptedException) {
                                onError(RuntimeException("다운로드 중단됨"))
                            }
                        }
                        downloadThread?.start()
                        
                        // 취소 함수 반환
                        return {
                            cancelled = true
                            downloadThread?.interrupt()
                        }
                    }
                }
                
                suspend fun downloadFile(url: String): ByteArray = 
                    suspendCancellableCoroutine { cont ->
                        val downloader = FileDownloader()
                        val progressList = mutableListOf<Int>()
                        
                        val cancelFn = downloader.startDownload(
                            url = url,
                            onProgress = { progress ->
                                progressList.add(progress)
                                println("다운로드 진행중: $progress%")
                            },
                            onComplete = { data ->
                                cont.resume(data)
                            },
                            onError = { error ->
                                cont.resumeWithException(error)
                            }
                        )
                        
                        // 🔑 취소 시 다운로드 중단
                        cont.invokeOnCancellation {
                            println("코루틴 취소 - 다운로드 중단 요청")
                            cancelFn()
                        }
                    }
                
                val downloadJob = launch {
                    try {
                        val data = downloadFile("https://example.com/large-file.zip")
                        println("다운로드 완료: ${data.size} bytes")
                    } catch (e: CancellationException) {
                        println("다운로드가 취소되었습니다")
                        throw e
                    }
                }
                
                delay(250) // 30% 정도 진행
                downloadJob.cancel() // 취소!
                downloadJob.join()
                
                downloadJob.isCancelled shouldBe true
            }
        }
    }

    given("이벤트 리스너 관리") {
        `when`("이벤트 리스너를 코루틴으로 관리할 때") {
            then("취소 시 리스너가 자동으로 해제된다") {
                class EventBus {
                    private val listeners = mutableMapOf<String, MutableList<(String) -> Unit>>()
                    
                    @Synchronized
                    fun subscribe(event: String, listener: (String) -> Unit): () -> Unit {
                        listeners.getOrPut(event) { mutableListOf() }.add(listener)
                        println("리스너 등록: $event")
                        
                        // unsubscribe 함수 반환
                        return {
                            synchronized(this) {
                                listeners[event]?.remove(listener)
                                println("리스너 해제: $event")
                            }
                        }
                    }
                    
                    @Synchronized
                    fun emit(event: String, data: String) {
                        // 리스너 목록 복사본을 만들어 iteration 중 수정 방지
                        val listenersCopy = listeners[event]?.toList() ?: emptyList()
                        listenersCopy.forEach { it(data) }
                    }
                    
                    @Synchronized
                    fun getListenerCount(event: String): Int = 
                        listeners[event]?.size ?: 0
                }
                
                suspend fun waitForEvent(bus: EventBus, eventName: String): String =
                    suspendCancellableCoroutine { cont ->
                        var unsubscribe: (() -> Unit)? = null
                        unsubscribe = bus.subscribe(eventName) { data ->
                            if (cont.isActive) {
                                cont.resume(data)
                                unsubscribe?.invoke() // 이벤트 수신 후 자동 해제
                            }
                        }
                        
                        // 🔑 취소 시 리스너 자동 해제
                        cont.invokeOnCancellation {
                            println("이벤트 대기 취소: $eventName")
                            unsubscribe?.invoke()
                        }
                    }
                
                val eventBus = EventBus()
                
                // 여러 이벤트 동시 대기
                val job1 = launch {
                    val data = waitForEvent(eventBus, "user.login")
                    println("로그인 이벤트 수신: $data")
                }
                
                val job2 = launch {
                    val data = waitForEvent(eventBus, "user.logout")
                    println("로그아웃 이벤트 수신: $data")
                }
                
                delay(100)
                
                // 리스너 등록 확인
                eventBus.getListenerCount("user.login") shouldBe 1
                eventBus.getListenerCount("user.logout") shouldBe 1
                
                // job1만 취소
                job1.cancel()
                delay(50)
                
                // job1의 리스너는 해제, job2는 유지
                eventBus.getListenerCount("user.login") shouldBe 0
                eventBus.getListenerCount("user.logout") shouldBe 1
                
                // job2에 이벤트 전송
                eventBus.emit("user.logout", "user123")
                job2.join()
                delay(50) // 리스너 해제 대기
                
                // 모든 리스너 해제됨 (이벤트 수신 후 자동 해제)
                eventBus.getListenerCount("user.logout") shouldBe 0
            }
        }
    }

    given("리소스 풀 관리") {
        `when`("제한된 리소스를 관리할 때") {
            then("취소 시 리소스가 풀로 반환된다") {
                class ResourcePool<T>(private val factory: () -> T, private val maxSize: Int) {
                    private val available = mutableListOf<T>()
                    private val inUse = mutableSetOf<T>()
                    
                    fun acquire(): T? {
                        return when {
                            available.isNotEmpty() -> {
                                val resource = available.removeAt(0)
                                inUse.add(resource)
                                println("리소스 획득: $resource")
                                resource
                            }
                            inUse.size < maxSize -> {
                                val resource = factory()
                                inUse.add(resource)
                                println("새 리소스 생성: $resource")
                                resource
                            }
                            else -> null
                        }
                    }
                    
                    fun release(resource: T) {
                        if (inUse.remove(resource)) {
                            available.add(resource)
                            println("리소스 반환: $resource")
                        }
                    }
                    
                    fun getAvailableCount() = available.size
                    fun getInUseCount() = inUse.size
                }
                
                suspend fun <T, R> withPooledResource(
                    pool: ResourcePool<T>,
                    block: suspend (T) -> R
                ): R {
                    val resource = pool.acquire() ?: throw RuntimeException("리소스 부족")
                    
                    return try {
                        suspendCancellableCoroutine { cont ->
                            // 🔑 취소 시 리소스 반환
                            cont.invokeOnCancellation {
                                println("작업 취소 - 리소스 반환")
                                pool.release(resource)
                            }
                            
                            GlobalScope.launch {
                                try {
                                    val result = block(resource)
                                    if (cont.isActive) {
                                        cont.resume(result)
                                    }
                                } catch (e: CancellationException) {
                                    // 취소는 invokeOnCancellation에서 처리
                                    throw e
                                } catch (e: Exception) {
                                    if (cont.isActive) {
                                        cont.resumeWithException(e)
                                    }
                                }
                            }
                        }
                    } finally {
                        pool.release(resource)
                    }
                }
                
                // 연결 풀 생성 (최대 2개)
                var connectionId = 0
                val connectionPool = ResourcePool(
                    factory = { "Connection-${++connectionId}" },
                    maxSize = 2
                )
                
                val jobs = List(3) { index ->
                    launch {
                        try {
                            withPooledResource(connectionPool) { conn ->
                                println("작업 $index: $conn 사용 중")
                                delay(200)
                                "작업 $index 완료"
                            }
                        } catch (e: Exception) {
                            println("작업 $index 실패: ${e.message}")
                        }
                    }
                }
                
                delay(100) // 처음 2개 작업이 리소스 획득
                connectionPool.getInUseCount() shouldBe 2
                connectionPool.getAvailableCount() shouldBe 0
                
                // 첫 번째 작업 취소
                jobs[0].cancel()
                delay(100) // 리소스 반환 및 세 번째 작업 시작 대기
                
                // 취소된 작업의 리소스가 반환되어 세 번째 작업이 진행 중이거나 완료
                // 정확한 상태는 타이밍에 따라 달라질 수 있음
                val inUse = connectionPool.getInUseCount()
                println("현재 사용 중인 리소스: $inUse")
                (inUse == 1 || inUse == 2) shouldBe true
                
                jobs.forEach { it.join() }
                
                // 모든 작업 완료 후
                connectionPool.getInUseCount() shouldBe 0
                connectionPool.getAvailableCount() shouldBe 2
            }
        }
    }

    given("체이닝된 비동기 작업") {
        `when`("여러 단계의 비동기 작업을 수행할 때") {
            then("중간 단계에서 취소 시 모든 정리 작업이 순서대로 실행된다") {
                val cleanupOrder = mutableListOf<String>()
                
                suspend fun step1(): String = suspendCancellableCoroutine { cont ->
                    val timer = Timer()
                    timer.schedule(100) {
                        cont.resume("Step1 완료")
                        timer.cancel()
                    }
                    
                    cont.invokeOnCancellation {
                        cleanupOrder.add("Step1 정리")
                        timer.cancel()
                    }
                }
                
                suspend fun step2(input: String): String = suspendCancellableCoroutine { cont ->
                    val timer = Timer()
                    timer.schedule(100) {
                        cont.resume("$input -> Step2 완료")
                        timer.cancel()
                    }
                    
                    cont.invokeOnCancellation {
                        cleanupOrder.add("Step2 정리")
                        timer.cancel()
                    }
                }
                
                suspend fun step3(input: String): String = suspendCancellableCoroutine { cont ->
                    val timer = Timer()
                    timer.schedule(100) {
                        cont.resume("$input -> Step3 완료")
                        timer.cancel()
                    }
                    
                    cont.invokeOnCancellation {
                        cleanupOrder.add("Step3 정리")
                        timer.cancel()
                    }
                }
                
                val job = launch {
                    try {
                        val result1 = step1()
                        println("Step1: $result1")
                        
                        val result2 = step2(result1)
                        println("Step2: $result2")
                        
                        val result3 = step3(result2)
                        println("Step3: $result3")
                    } catch (e: CancellationException) {
                        println("작업 체인 취소됨")
                        throw e
                    }
                }
                
                delay(150) // Step2 진행 중
                job.cancel()
                job.join()
                
                // 현재 실행 중이던 단계만 정리됨
                cleanupOrder shouldBe listOf("Step2 정리")
            }
        }
    }

    given("취소 시점 세밀 제어") {
        `when`("isActive를 사용하여 취소 확인할 때") {
            then("취소 후에는 resume이 무시된다") {
                var resumeCount = 0
                var cancelCount = 0
                
                suspend fun cancellableWork(): String = suspendCancellableCoroutine { cont ->
                    val executor = Executors.newSingleThreadExecutor()
                    
                    executor.submit {
                        Thread.sleep(100)
                        
                        // 🔑 isActive 체크로 불필요한 resume 방지
                        if (cont.isActive) {
                            resumeCount++
                            cont.resume("작업 완료")
                        } else {
                            println("이미 취소됨 - resume 생략")
                        }
                    }
                    
                    cont.invokeOnCancellation {
                        cancelCount++
                        executor.shutdownNow()
                        println("Executor 종료")
                    }
                }
                
                val job = launch {
                    cancellableWork()
                }
                
                delay(50) // 작업 진행 중
                job.cancel()
                job.join()
                
                delay(100) // executor의 작업이 완료될 시간
                
                resumeCount shouldBe 0 // isActive 체크로 resume 방지됨
                cancelCount shouldBe 1 // 취소 핸들러는 실행됨
            }
        }
    }
})
