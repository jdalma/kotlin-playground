package _30_Coroutines

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.nulls.shouldBeNull
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.ContinuationInterceptor

/**
 * coroutineScope vs CoroutineScope() 차이점을 명확하게 보여주는 테스트
 * - 컨텍스트 상속 vs 독립성
 * - 구조적 관계 vs 완전 분리
 * - 예외/취소 전파 차이
 */
class CoroutineScopeTest : BehaviorSpec({

    given("coroutineScope 함수를 사용할 때") {
        `when`("부모 코루틴의 컨텍스트를 확인하면") {
            then("Job을 제외한 모든 컨텍스트를 상속받아야 한다") {
                runBlocking(CoroutineName("ParentCoroutine") + Dispatchers.IO) {
                    val parentJob = currentCoroutineContext()[Job]
                    val parentName = currentCoroutineContext()[CoroutineName]
                    val parentDispatcher = currentCoroutineContext()[ContinuationInterceptor]

                    coroutineScope {
                        val childJob = currentCoroutineContext()[Job]
                        val childName = currentCoroutineContext()[CoroutineName]
                        val childDispatcher = currentCoroutineContext()[ContinuationInterceptor]

                        // Job은 다르지만 (새로운 자식 Job)
                        childJob shouldNotBe parentJob
                        childJob?.parent shouldBe parentJob // 부모-자식 관계

                        // 다른 컨텍스트는 상속
                        childName shouldBe parentName // CoroutineName 상속
                        childDispatcher shouldBe parentDispatcher // Dispatcher 상속
                    }
                }
            }
        }

        `when`("내부에서 예외가 발생하면") {
            then("부모로 예외가 전파되어야 한다") {
                val exceptionCaught = AtomicReference<String>()

                try {
                    runBlocking {
                        coroutineScope {
                            launch { 
                                delay(50)
                                throw RuntimeException("coroutineScope 내부 예외") 
                            }
                            launch { 
                                delay(100)
                                // 이 코루틴은 실행되지 않음 (형제 예외로 취소)
                            }
                        }
                    }
                } catch (e: RuntimeException) {
                    exceptionCaught.set(e.message)
                }

                exceptionCaught.get() shouldBe "coroutineScope 내부 예외"
            }
        }

        `when`("부모 코루틴이 취소되면") {
            then("함께 취소되어야 한다") {
                val childCancelled = AtomicInteger(0)
                
                val parentJob = GlobalScope.launch {
                    coroutineScope {
                        launch {
                            try {
                                delay(1000)
                            } catch (e: CancellationException) {
                                childCancelled.incrementAndGet()
                                throw e
                            }
                        }
                        
                        launch {
                            try {
                                delay(1000)
                            } catch (e: CancellationException) {
                                childCancelled.incrementAndGet()
                                throw e
                            }
                        }
                    }
                }

                delay(100)
                parentJob.cancel() // 부모 취소
                parentJob.join()

                childCancelled.get() shouldBe 2 // 두 자식 모두 취소됨
            }
        }
    }

    given("CoroutineScope() 생성자를 사용할 때") {
        `when`("독립적인 스코프의 컨텍스트를 확인하면") {
            then("부모와 완전히 독립적인 컨텍스트를 가져야 한다") {
                runBlocking(CoroutineName("ParentCoroutine") + Dispatchers.IO) {
                    val parentJob = currentCoroutineContext()[Job]
                    val parentName = currentCoroutineContext()[CoroutineName]

                    val independentScope = CoroutineScope(
                        Job() + CoroutineName("IndependentCoroutine") + Dispatchers.Default
                    )

                    val contextResult = AtomicReference<Triple<Job?, CoroutineName?, ContinuationInterceptor?>>()

                    independentScope.launch {
                        val independentJob = currentCoroutineContext()[Job]
                        val independentName = currentCoroutineContext()[CoroutineName]
                        val independentDispatcher = currentCoroutineContext()[ContinuationInterceptor]

                        contextResult.set(Triple(independentJob, independentName, independentDispatcher))
                    }.join()

                    val (independentJob, independentName, independentDispatcher) = contextResult.get()

                    // 완전히 독립적
                    independentJob shouldNotBe parentJob
                    independentJob?.parent.shouldBeNull() // 부모 없음
                    
                    independentName shouldNotBe parentName
                    independentName?.name shouldBe "IndependentCoroutine"
                    
                    independentDispatcher shouldNotBe currentCoroutineContext()[ContinuationInterceptor]

                    independentScope.cancel()
                }
            }
        }

        `when`("독립 스코프에서 예외가 발생하면") {
            then("부모에게 예외가 전파되지 않아야 한다") {
                var parentCompleted = false
                val exceptionHandled = AtomicReference<String>()

                val handler = CoroutineExceptionHandler { _, exception ->
                    exceptionHandled.set(exception.message)
                }

                runBlocking {
                    val independentScope = CoroutineScope(SupervisorJob() + handler)
                    
                    independentScope.launch {
                        delay(50)
                        throw RuntimeException("독립 스코프 예외")
                    }

                    delay(100) // 예외 발생 대기
                    parentCompleted = true // 부모는 정상 완료

                    independentScope.cancel()
                }

                parentCompleted shouldBe true
                exceptionHandled.get() shouldBe "독립 스코프 예외"
            }
        }

        `when`("부모가 취소되어도") {
            then("독립 스코프는 영향받지 않아야 한다") {
                val independentScopeCompleted = AtomicInteger(0)
                val parentJob = GlobalScope.launch {
                    val independentScope = CoroutineScope(SupervisorJob())
                    
                    // 독립 스코프에서 장기 실행 작업 시작
                    independentScope.launch {
                        try {
                            delay(200)
                            independentScopeCompleted.incrementAndGet()
                        } catch (e: CancellationException) {
                            // 독립적이므로 취소되지 않아야 함
                        }
                    }
                    
                    independentScope.launch {
                        try {
                            delay(300)
                            independentScopeCompleted.incrementAndGet()
                        } catch (e: CancellationException) {
                            // 독립적이므로 취소되지 않아야 함
                        }
                    }
                    
                    delay(100) // 잠깐 실행 후 부모 종료
                    // 이때 independentScope는 계속 실행되어야 함
                }

                // 부모 job 취소
                delay(150)
                parentJob.cancel()
                parentJob.join()

                // 독립 스코프는 계속 실행됨
                delay(200)
                
                independentScopeCompleted.get() shouldBe 2 // 두 작업 모두 완료
            }
        }
    }

    given("컨텍스트 상속과 독립성을 직접 비교할 때") {
        `when`("같은 부모에서 두 방식을 사용하면") {
            then("상속 vs 독립성의 차이가 명확해야 한다") {
                runBlocking(CoroutineName("TestParent") + Dispatchers.IO) {
                    val parentJob = currentCoroutineContext()[Job]
                    val parentName = currentCoroutineContext()[CoroutineName]
                    val parentDispatcher = currentCoroutineContext()[ContinuationInterceptor]

                    // 1. coroutineScope - 상속받는 방식
                    var scopeJob: Job? = null
                    var scopeParent: Job? = null
                    var scopeName: CoroutineName? = null
                    var scopeDispatcher: ContinuationInterceptor? = null

                    coroutineScope {
                        scopeJob = currentCoroutineContext()[Job]
                        scopeParent = currentCoroutineContext()[Job]?.parent
                        scopeName = currentCoroutineContext()[CoroutineName]
                        scopeDispatcher = currentCoroutineContext()[ContinuationInterceptor]
                    }

                    // 2. CoroutineScope() - 독립적인 방식
                    var independentJob: Job? = null
                    var independentParent: Job? = null
                    var independentName: CoroutineName? = null
                    var independentDispatcher: ContinuationInterceptor? = null

                    val independentParentJob = Job()
                    val independent = CoroutineScope( independentParentJob + CoroutineName("Independent"))
                    independent.launch {
                        independentJob = currentCoroutineContext()[Job]
                        independentParent = currentCoroutineContext()[Job]?.parent
                        independentName = currentCoroutineContext()[CoroutineName]
                        independentDispatcher = currentCoroutineContext()[ContinuationInterceptor]
                    }.join()

                    // 검증: coroutineScope는 상속받음
                    scopeJob shouldNotBe parentJob          // Job은 새로 생성
                    scopeParent shouldBe parentJob          // 하지만 부모는 원래 Job
                    scopeName shouldBe parentName           // 이름 상속 (TestParent)
                    scopeDispatcher shouldBe parentDispatcher // Dispatcher 상속 (IO)

                    // 검증: CoroutineScope()는 독립적
                    independentJob shouldNotBe parentJob    // Job은 완전히 다름
                    independentParent shouldNotBe parentJob   // 부모 Job도 서로 다름
                    independentParent shouldBe independentParentJob // 지정한 부모 Job
                    independentName?.name shouldBe "Independent"  // 지정한 이름
                    independentDispatcher shouldNotBe parentDispatcher // 기본 Dispatcher

                    // 부모-자식 관계 명확히 확인
                    println("=== 컨텍스트 비교 ===")
                    println("Parent: Job=${parentJob?.hashCode()}, Name=${parentName?.name}")
                    println("coroutineScope: Job=${scopeJob?.hashCode()}, Parent=${scopeParent?.hashCode()}, Name=${scopeName?.name}")
                    println("CoroutineScope(): Job=${independentJob?.hashCode()}, Parent=${independentParent?.hashCode()}, Name=${independentName?.name}")

                    independent.cancel()
                }
            }
        }
    }

    given("실제 사용 시나리오에서") {
        `when`("구조화된 병렬 작업이 필요하면") {
            then("coroutineScope를 사용해야 한다") {
                data class UserData(val profile: String, val posts: List<String>, val friends: List<String>)
                
                suspend fun fetchUserData(userId: String): UserData = coroutineScope {
                    val profile = async { 
                        delay(100)
                        "Profile-$userId"
                    }
                    val posts = async { 
                        delay(150)
                        listOf("Post1", "Post2")
                    }
                    val friends = async { 
                        delay(120)
                        listOf("Friend1", "Friend2")
                    }

                    UserData(
                        profile = profile.await(),
                        posts = posts.await(),
                        friends = friends.await()
                    )
                }

                runBlocking {
                    val userData = fetchUserData("user123")
                    
                    userData.profile shouldBe "Profile-user123"
                    userData.posts.size shouldBe 2
                    userData.friends.size shouldBe 2
                }
            }
        }

        `when`("독립적인 백그라운드 서비스가 필요하면") {
            then("CoroutineScope()를 사용해야 한다") {
                class EmailService {
                    private val emailScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
                    private val sentEmails = mutableListOf<String>()
                    
                    fun sendEmailAsync(email: String) {
                        emailScope.launch {
                            delay(50) // 이메일 발송 시뮬레이션
                            sentEmails.add(email)
                        }
                    }
                    
                    suspend fun getSentEmails(): List<String> {
                        delay(1000) // 모든 이메일 발송 완료 대기
                        return sentEmails.toList()
                    }
                    
                    fun shutdown() {
                        emailScope.cancel()
                    }
                }

                runBlocking {
                    val emailService = EmailService()
                    
                    // 여러 이메일 비동기 발송 (Fire and Forget)
                    emailService.sendEmailAsync("test1@example.com")
                    emailService.sendEmailAsync("test2@example.com")
                    emailService.sendEmailAsync("test3@example.com")
                    
                    // 발송 완료 확인
                    val sentEmails = emailService.getSentEmails()
                    sentEmails.size shouldBe 3
                    
                    emailService.shutdown()
                }
            }
        }
    }
})
