package _30_Coroutines

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * 코루틴 빌더와 스코프 함수별 예외 전파 메커니즘 통합 테스트
 * - 기존 중복 테스트들을 통합하여 체계적으로 정리
 * - 각 빌더/스코프별 예외 전파 방식을 명확히 구분
 */
class CoroutineExceptionPropagationTest : BehaviorSpec({

    given("코루틴 빌더별 예외 처리 방식") {
        `when`("launch 빌더를 사용할 때") {
            then("예외가 Job 계층을 통해 전파된다 (Uncaught Exception)") {
                val exceptionHandled = AtomicBoolean(false)
                val handler = CoroutineExceptionHandler { _, exception ->
                    exceptionHandled.set(true)
                    println("Launch 예외 처리: ${exception.message}")
                }
                
                runBlocking {
                    val scope = CoroutineScope(Job() + handler)
                    
                    scope.launch {
                        delay(50)
                        throw RuntimeException("Launch 예외")
                    }.join()
                    
                    exceptionHandled.get() shouldBe true
                }
            }
        }

        `when`("supervisorScope 내 async 빌더를 사용할 때") {
            then("예외가 캡슐화되어 await() 시점에 노출된다") {
                runBlocking {
                    supervisorScope {
                        val deferred = async {
                            delay(50)
                            throw IllegalStateException("Async 예외")
                        }
                        
                        // async 완료되어도 예외는 아직 던져지지 않음
                        deferred.join()
                        deferred.isCompleted shouldBe true
                        
                        // await() 호출 시 예외 발생
                        shouldThrow<IllegalStateException> {
                            deferred.await()
                        }
                    }
                }
            }
        }
    }

    given("스코프 함수별 예외 처리 방식") {
        `when`("runBlocking을 사용할 때") {
            then("내부 예외가 호출자에게 직접 전파된다") {
                shouldThrow<RuntimeException> {
                    runBlocking {
                        launch {
                            delay(50)
                            throw RuntimeException("runBlocking 내부 예외")
                        }
                    }
                }
            }
        }

        `when`("coroutineScope를 사용할 때") {
            then("내부 예외가 호출자에게 직접 전파된다") {
                runBlocking {
                    shouldThrow<IllegalArgumentException> {
                        coroutineScope {
                            launch {
                                delay(50)
                                throw IllegalArgumentException("coroutineScope 내부 예외")
                            }
                        }
                    }
                }
            }
        }

        `when`("supervisorScope를 사용할 때") {
            then("자식 예외가 다른 자식에게 전파되지 않는다") {
                val child1Failed = AtomicBoolean(false)
                val child2Completed = AtomicBoolean(false)
                val scopeCompleted = AtomicBoolean(false)
                
                runBlocking {
                    supervisorScope {
                        launch {
                            delay(50)
                            child1Failed.set(true)
                            throw RuntimeException("자식 1 예외")
                        }
                        
                        launch {
                            delay(100)
                            child2Completed.set(true)
                        }
                        
                        delay(150)
                        scopeCompleted.set(true)
                    }
                }
                
                child1Failed.get() shouldBe true
                child2Completed.get() shouldBe true
                scopeCompleted.get() shouldBe true
            }
        }

        `when`("withContext를 사용할 때") {
            then("내부 예외가 호출자에게 직접 전파된다") {
                runBlocking {
                    shouldThrow<RuntimeException> {
                        withContext(Dispatchers.Default) {
                            delay(50)
                            throw RuntimeException("withContext 내부 예외")
                        }
                    }
                }
            }
        }
    }

    given("독립적인 스코프 생성 시") {
        `when`("CoroutineScope()로 독립 스코프를 만들 때") {
            then("예외가 부모로 전파되지 않는다") {
                val parentCompleted = AtomicBoolean(false)
                val childFailed = AtomicBoolean(false)
                
                runBlocking {
                    launch {
                        val independentScope = CoroutineScope(Job())
                        
                        independentScope.launch {
                            delay(50)
                            childFailed.set(true)
                            throw RuntimeException("독립 스코프 예외")
                        }
                        
                        delay(100)
                        parentCompleted.set(true)
                    }.join()
                }
                
                childFailed.get() shouldBe true
                parentCompleted.get() shouldBe true
            }
        }

        `when`("SupervisorJob으로 독립 스코프를 만들 때") {
            then("자식 간 예외 전파도 방지된다") {
                val child1Failed = AtomicBoolean(false)
                val child2Completed = AtomicBoolean(false)
                val parentCompleted = AtomicBoolean(false)
                
                runBlocking {
                    launch {
                        val supervisorScope = CoroutineScope(SupervisorJob())
                        
                        supervisorScope.launch {
                            delay(50)
                            child1Failed.set(true)
                            throw RuntimeException("SupervisorJob 자식 1 예외")
                        }
                        
                        supervisorScope.launch {
                            delay(100)
                            child2Completed.set(true)
                        }
                        
                        delay(150)
                        parentCompleted.set(true)
                    }.join()
                }
                
                child1Failed.get() shouldBe true
                child2Completed.get() shouldBe true
                parentCompleted.get() shouldBe true
            }
        }
    }

    given("특수한 예외 전파 케이스") {
        `when`("runBlocking의 직접 자식 async에서 예외가 발생하면") {
            then("await() 없이도 즉시 전파된다") {
                shouldThrow<IllegalStateException> {
                    runBlocking {
                        async {
                            delay(50)
                            throw IllegalStateException("runBlocking 자식 async 예외")
                        }
                        delay(100) // 도달하지 못함
                    }
                }
            }
        }

        `when`("launch의 자식 async에서 예외가 발생하면") {
            then("launch 계층으로 즉시 전파된다") {
                val exceptionHandled = AtomicBoolean(false)
                val handler = CoroutineExceptionHandler { _, _ ->
                    exceptionHandled.set(true)
                }
                
                runBlocking {
                    val scope = CoroutineScope(Job() + handler)
                    
                    scope.launch {
                        async {
                            delay(50)
                            throw RuntimeException("launch 자식 async 예외")
                        }
                        delay(100)
                    }.join()
                    
                    exceptionHandled.get() shouldBe true
                }
            }
        }

        `when`("supervisorScope 내 async에서 예외가 발생하면") {
            then("await() 호출 전까지 예외가 보류된다") {
                runBlocking {
                    supervisorScope {
                        val rootAsync = async {
                            delay(50)
                            throw RuntimeException("supervisorScope async 예외")
                        }
                        
                        delay(100) // 예외 발생 후에도 정상 진행
                        rootAsync.isCompleted shouldBe true
                        
                        shouldThrow<RuntimeException> {
                            rootAsync.await()
                        }
                    }
                }
            }
        }
    }

    given("구조화된 동시성과 예외 전파") {
        `when`("자식 코루틴에서 예외가 발생하면") {
            then("모든 형제 코루틴이 취소된다") {
                val siblingsCount = AtomicInteger(0)
                val cancelledCount = AtomicInteger(0)
                val handler = CoroutineExceptionHandler { _, _ -> }
                
                runBlocking {
                    val scope = CoroutineScope(Job() + handler)
                    
                    scope.launch {
                        // 예외 발생하는 자식
                        launch {
                            delay(50)
                            throw RuntimeException("자식 예외")
                        }
                        
                        // 형제들 - 모두 취소됨
                        repeat(3) {
                            launch {
                                siblingsCount.incrementAndGet()
                                try {
                                    delay(1000)
                                } catch (e: CancellationException) {
                                    cancelledCount.incrementAndGet()
                                    throw e
                                }
                            }
                        }
                        
                        delay(1000)
                    }.join()
                }
                
                siblingsCount.get() shouldBe 3
                cancelledCount.get() shouldBe 3
            }
        }
    }

    given("CoroutineExceptionHandler 동작") {
        `when`("launch에서 사용하면") {
            then("예외를 처리한다") {
                var handledException: Throwable? = null
                val handler = CoroutineExceptionHandler { _, exception ->
                    handledException = exception
                }
                
                runBlocking {
                    GlobalScope.launch(handler) {
                        throw ArithmeticException("Division by zero")
                    }.join()
                    
                    delay(50)
                    handledException?.message shouldBe "Division by zero"
                }
            }
        }

        `when`("async에서 사용하면") {
            then("await() 호출 시에는 핸들러가 동작하지 않는다") {
                var handlerCalled = false
                val handler = CoroutineExceptionHandler { _, _ ->
                    handlerCalled = true
                }
                
                runBlocking {
                    // supervisorScope.async + await() 시에는 핸들러 미동작
                    try {
                        supervisorScope {
                            val deferred = async {
                                throw RuntimeException("async 예외")
                            }
                            deferred.await() // await() 호출로 예외가 노출되면 핸들러 미동작
                        }
                    } catch (e: RuntimeException) {
                        // 예외는 await()에서 처리됨
                    }
                    
                    delay(50)
                    handlerCalled shouldBe false // await() 호출하면 핸들러 호출되지 않음
                }
            }
        }
    }

    given("실제 사용 시나리오") {
        `when`("병렬 데이터 로딩 중 일부 실패 시") {
            then("실패한 부분만 폴백 처리하고 나머지는 정상 진행") {
                suspend fun loadUserData(): Pair<String, String> = supervisorScope {
                    val userData = async {
                        delay(50)
                        throw RuntimeException("사용자 데이터 로딩 실패")
                    }
                    
                    val settingsData = async {
                        delay(30)
                        "사용자 설정"
                    }
                    
                    val userResult = try {
                        userData.await()
                    } catch (e: RuntimeException) {
                        "기본 사용자"
                    }
                    
                    val settingsResult = settingsData.await()
                    userResult to settingsResult
                }
                
                runBlocking {
                    val (user, settings) = loadUserData()
                    user shouldBe "기본 사용자"
                    settings shouldBe "사용자 설정"
                }
            }
        }

        `when`("백그라운드 작업과 메인 작업을 분리할 때") {
            then("백그라운드 작업 실패가 메인 작업에 영향을 주지 않는다") {
                val mainCompleted = AtomicBoolean(false)
                val backgroundFailed = AtomicBoolean(false)
                
                runBlocking {
                    launch {
                        // 독립적인 백그라운드 스코프
                        val backgroundScope = CoroutineScope(SupervisorJob())
                        
                        backgroundScope.launch {
                            delay(30)
                            backgroundFailed.set(true)
                            throw RuntimeException("백그라운드 작업 실패")
                        }
                        
                        // 메인 작업
                        delay(100)
                        mainCompleted.set(true)
                    }.join()
                }
                
                backgroundFailed.get() shouldBe true
                mainCompleted.get() shouldBe true
            }
        }
    }

    given("예외 처리 패턴 비교") {
        `when`("try-catch로 감쌀 수 있는 경우") {
            then("직접 처리가 가능하다") {
                runBlocking {
                    // coroutineScope - 가능
                    var caught1 = false
                    try {
                        coroutineScope {
                            throw RuntimeException("coroutineScope 예외")
                        }
                    } catch (e: RuntimeException) {
                        caught1 = true
                    }
                    
                    // withContext - 가능  
                    var caught2 = false
                    try {
                        withContext(Dispatchers.Default) {
                            throw RuntimeException("withContext 예외")
                        }
                    } catch (e: RuntimeException) {
                        caught2 = true
                    }
                    
                    // supervisorScope.async.await() - 가능
                    var caught3 = false
                    try {
                        supervisorScope {
                            async {
                                throw RuntimeException("async 예외")
                            }.await()
                        }
                    } catch (e: RuntimeException) {
                        caught3 = true
                    }
                    
                    caught1 shouldBe true
                    caught2 shouldBe true
                    caught3 shouldBe true
                }
            }
        }

        `when`("try-catch로 감쌀 수 없는 경우") {
            then("CoroutineExceptionHandler가 필요하다") {
                val handlerUsed = AtomicBoolean(false)
                val handler = CoroutineExceptionHandler { _, _ ->
                    handlerUsed.set(true)
                }
                
                runBlocking {
                    // launch - try-catch로 감쌀 수 없음
                    var caught = false
                    try {
                        GlobalScope.launch(handler) {
                            throw RuntimeException("launch 예외")
                        }.join()
                    } catch (e: RuntimeException) {
                        caught = true
                    }
                    
                    delay(50)
                    caught shouldBe false
                    handlerUsed.get() shouldBe true
                }
            }
        }
    }
})
