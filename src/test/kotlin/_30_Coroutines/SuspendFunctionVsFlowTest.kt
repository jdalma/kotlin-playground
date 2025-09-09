package _30_Coroutines

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * 중단함수 vs Flow 비교 테스트
 * 
 * Kotlin Coroutine Expert 관점:
 * - 중단함수: 단일 값, 일회성 작업
 * - Flow: 다중 값, 스트림 처리, 백프레셔 제어
 * - 상황에 따른 적절한 선택이 중요
 * 
 * Unit Test Expert 관점:
 * - 각 접근법의 장단점을 실제 시나리오로 검증
 * - 성능, 취소, 에러 처리 차이점 확인
 */
class SuspendFunctionVsFlowTest : BehaviorSpec({

    given("단일 값 처리 시나리오") {
        `when`("중단함수를 사용하면") {
            then("간단하고 직관적인 구현이 가능하다") {
                suspend fun loadUserProfile(userId: String): UserProfile {
                    delay(100) // 네트워크 시뮬레이션
                    return UserProfile(userId, "User $userId", "user$userId@email.com")
                }
                
                runBlocking {
                    val profile = loadUserProfile("123")
                    
                    profile.id shouldBe "123"
                    profile.name shouldBe "User 123"
                    profile.email shouldBe "user123@email.com"
                }
            }
        }
        
        `when`("Flow를 사용하면") {
            then("오버헤드가 있지만 확장성이 좋다") {
                fun loadUserProfileFlow(userId: String): Flow<UserProfile> = flow {
                    delay(100)
                    emit(UserProfile(userId, "User $userId", "user$userId@email.com"))
                }
                
                runBlocking {
                    val profile = loadUserProfileFlow("123").single()
                    
                    profile.id shouldBe "123"
                    profile.name shouldBe "User 123"
                    profile.email shouldBe "user123@email.com"
                }
            }
        }
    }
    
    given("다중 값 처리 시나리오") {
        `when`("중단함수로 여러 값을 처리하면") {
            then("컬렉션을 반환하지만 메모리 사용량이 높다") {
                suspend fun loadAllUsers(): List<User> {
                    delay(100)
                    return (1..1000).map { User("user$it", "User $it") }
                }
                
                runBlocking {
                    val users = loadAllUsers()
                    
                    users.size shouldBe 1000
                    users.first().id shouldBe "user1"
                    users.last().id shouldBe "user1000"
                    
                    // 모든 데이터가 메모리에 로드됨
                }
            }
        }
        
        `when`("Flow로 여러 값을 처리하면") {
            then("스트림 방식으로 메모리 효율적 처리가 가능하다") {
                fun loadUsersFlow(): Flow<User> = flow {
                    repeat(1000) { i ->
                        delay(1) // 실제로는 DB/네트워크에서 하나씩 가져옴
                        emit(User("user${i + 1}", "User ${i + 1}"))
                    }
                }
                
                runBlocking {
                    val processedUsers = mutableListOf<String>()
                    
                    loadUsersFlow()
                        .take(5) // 처음 5개만 처리
                        .collect { user ->
                            processedUsers.add(user.id)
                        }
                    
                    processedUsers.size shouldBe 5
                    processedUsers shouldContain "user1"
                    processedUsers shouldContain "user5"
                    
                    // 전체 1000개가 아닌 5개만 처리됨
                }
            }
        }
    }
    
    given("실시간 데이터 시나리오") {
        `when`("중단함수로 실시간 데이터를 처리하려 하면") {
            then("폴링 방식으로 구현해야 하고 비효율적이다") {
                suspend fun getCurrentTemperature(): Double {
                    delay(50)
                    return (20..35).random().toDouble()
                }
                
                runBlocking {
                    val temperatures = mutableListOf<Double>()
                    
                    // 폴링 방식
                    repeat(5) {
                        temperatures.add(getCurrentTemperature())
                        delay(100)
                    }
                    
                    temperatures.size shouldBe 5
                    temperatures.all { it in 20.0..35.0 } shouldBe true
                }
            }
        }
        
        `when`("Flow로 실시간 데이터를 처리하면") {
            then("자연스러운 스트림 처리가 가능하다") {
                fun temperatureUpdates(): Flow<Double> = flow {
                    repeat(5) {
                        delay(100)
                        emit((20..35).random().toDouble())
                    }
                }
                
                runBlocking {
                    val temperatures = temperatureUpdates()
                        .toList()
                    
                    temperatures.size shouldBe 5
                    temperatures.all { it in 20.0..35.0 } shouldBe true
                }
            }
        }
    }
    
    given("취소 처리 시나리오") {
        `when`("중단함수에서 취소가 발생하면") {
            then("진행 중인 작업이 중단된다") {
                suspend fun longRunningTask(): String {
                    repeat(10) { i ->
                        delay(100)
                        println("Processing step $i")
                    }
                    return "완료"
                }
                
                runBlocking {
                    val job = launch {
                        try {
                            longRunningTask()
                        } catch (e: CancellationException) {
                            println("작업이 취소되었습니다")
                            throw e
                        }
                    }
                    
                    delay(250) // 2-3단계 후 취소
                    job.cancel()
                    job.join()
                    
                    job.isCancelled shouldBe true
                }
            }
        }
        
        `when`("Flow에서 취소가 발생하면") {
            then("스트림 처리가 중단되고 리소스가 정리된다") {
                fun longRunningStream(): Flow<Int> = flow {
                    try {
                        repeat(10) { i ->
                            delay(100)
                            println("Emitting $i")
                            emit(i)
                        }
                    } catch (e: CancellationException) {
                        println("Flow가 취소되었습니다")
                        throw e
                    }
                }
                
                runBlocking {
                    val results = mutableListOf<Int>()
                    
                    val job = launch {
                        longRunningStream()
                            .collect { value ->
                                results.add(value)
                            }
                    }
                    
                    delay(250) // 2-3개 값 수집 후 취소
                    job.cancel()
                    job.join()
                    
                    results.size shouldBe 2 // 0, 1만 수집됨
                    job.isCancelled shouldBe true
                }
            }
        }
    }
    
    given("에러 처리 시나리오") {
        `when`("중단함수에서 에러가 발생하면") {
            then("호출자에게 즉시 전파된다") {
                suspend fun errorProneTask(shouldFail: Boolean): String {
                    delay(100)
                    if (shouldFail) {
                        throw IllegalStateException("작업 실패")
                    }
                    return "성공"
                }
                
                runBlocking {
                    var errorCaught = false
                    
                    try {
                        errorProneTask(true)
                    } catch (e: IllegalStateException) {
                        errorCaught = true
                    }
                    
                    errorCaught shouldBe true
                }
            }
        }
        
        `when`("Flow에서 에러가 발생하면") {
            then("catch 연산자로 우아한 에러 처리가 가능하다") {
                fun errorProneFlow(): Flow<String> = flow {
                    emit("시작")
                    delay(100)
                    emit("진행 중")
                    delay(100)
                    throw IllegalStateException("중간 실패")
                    emit("완료") // 실행되지 않음
                }
                
                runBlocking {
                    val results = errorProneFlow()
                        .catch { exception ->
                            emit("에러 복구: ${exception.message}")
                        }
                        .toList()
                    
                    results.size shouldBe 3
                    results[0] shouldBe "시작"
                    results[1] shouldBe "진행 중"
                    results[2] shouldBe "에러 복구: 중간 실패"
                }
            }
        }
    }
    
    given("백프레셔 처리 시나리오") {
        `when`("빠른 생산자와 느린 소비자가 있을 때") {
            then("Flow는 백프레셔를 자연스럽게 처리한다") {
                fun fastProducer(): Flow<Int> = flow {
                    repeat(10) { i ->
                        println("생산: $i")
                        emit(i)
                        delay(10) // 빠른 생산
                    }
                }
                
                runBlocking {
                    val consumedCount = AtomicInteger(0)
                    
                    fastProducer()
                        .collect { value ->
                            println("소비: $value")
                            delay(100) // 느린 소비
                            consumedCount.incrementAndGet()
                        }
                    
                    consumedCount.get() shouldBe 10
                    // 생산자는 소비자를 기다림 (백프레셔)
                }
            }
        }
    }
    
    given("변환 및 조합 시나리오") {
        `when`("데이터 변환이 필요할 때") {
            then("Flow는 풍부한 연산자를 제공한다") {
                fun numbersFlow(): Flow<Int> = flowOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                
                runBlocking {
                    val result = numbersFlow()
                        .filter { it % 2 == 0 } // 짝수만
                        .map { it * it } // 제곱
                        .take(3) // 처음 3개
                        .toList()
                    
                    result shouldBe listOf(4, 16, 36) // 2², 4², 6²
                }
            }
        }
        
        `when`("여러 소스를 결합할 때") {
            then("Flow는 강력한 결합 연산자를 제공한다") {
                fun source1(): Flow<String> = flowOf("A", "B", "C").onEach { delay(100) }
                fun source2(): Flow<Int> = flowOf(1, 2, 3).onEach { delay(150) }
                
                runBlocking {
                    val combined = source1()
                        .zip(source2()) { str, num -> "$str$num" }
                        .toList()
                    
                    combined shouldBe listOf("A1", "B2", "C3")
                }
            }
        }
    }
})

data class UserProfile(val id: String, val name: String, val email: String)
data class User(val id: String, val name: String)