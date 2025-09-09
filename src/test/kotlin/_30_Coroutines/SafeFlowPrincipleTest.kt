package _30_Coroutines

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

/**
 * SafeFlow 원리를 이해하기 위한 테스트
 * 
 * Kotlin Coroutine Expert 관점:
 * - 수신자 타입(Receiver Type)을 통한 안전한 emit 보장
 * - FlowCollector 컨텍스트에서만 emit() 호출 가능
 * - 컴파일 타임에 잘못된 사용을 방지
 * 
 * Unit Test Expert 관점:
 * - 간단한 예제로 SafeFlow의 동작 원리 확인
 * - 수신자 함수의 특성을 명확히 검증
 */
class SafeFlowPrincipleTest : BehaviorSpec({

    given("SafeFlow 원리 이해를 위한 첫 번째 예제") {
        `when`("수신자 타입을 사용하지 않은 일반적인 방식") {
            then("emit이 어디서든 호출 가능해서 위험하다") {
                class UnsafeFlowExample<T>(
                    private val block: suspend () -> Unit  // ❌ 잘못된 방식 - 수신자 타입 없음
                ) {
                    suspend fun collect(collector: FlowCollector<T>) {
                        // collector를 어떻게 block 안에서 사용할지 명확하지 않음
                        block() // collector 접근 불가!
                    }
                }
                
                runBlocking {
                    var emittedValue: String? = null
                    
                    // 실제로는 이런 식으로는 emit할 수 없음을 보여주는 예제
                    val collector = object : FlowCollector<String> {
                        override suspend fun emit(value: String) {
                            emittedValue = value
                        }
                    }
                    
                    val unsafeFlow = UnsafeFlowExample<String> {
                        // 여기서 emit을 호출하고 싶지만 방법이 없음!
                        // emit("Hello") // ❌ 컴파일 에러
                        println("block 실행됨")
                    }
                    
                    unsafeFlow.collect(collector)
                    
                    // emit이 호출되지 않았으므로 null
                    emittedValue shouldBe null
                }
            }
        }
        
        `when`("수신자 타입을 사용한 SafeFlow 방식") {
            then("FlowCollector 컨텍스트에서만 emit 호출이 가능하다") {
                // ✅ 올바른 방식 - 수신자 타입 사용
                class SafeFlowExample<T>(
                    private val block: suspend FlowCollector<T>.() -> Unit  // 수신자 타입 지정!
                ) {
                    suspend fun collect(collector: FlowCollector<T>) {
                        collector.block()  // collector를 수신자로 block 실행
                    }
                }
                
                runBlocking {
                    var emittedValue: String? = null
                    
                    val collector = object : FlowCollector<String> {
                        override suspend fun emit(value: String) {
                            emittedValue = value
                            println("값 수신: $value")
                        }
                    }
                    
                    val safeFlow = SafeFlowExample<String> {
                        // 이제 emit을 자유롭게 호출할 수 있음!
                        emit("Hello SafeFlow!") // ✅ 가능!
                        // this는 FlowCollector<String> 타입
                    }
                    
                    safeFlow.collect(collector)
                    
                    emittedValue shouldBe "Hello SafeFlow!"
                }
            }
        }
    }
    
    given("SafeFlow 원리 이해를 위한 두 번째 예제") {
        `when`("실제 flow { } 빌더와 동일한 방식 구현") {
            then("수신자 타입으로 안전하고 직관적인 Flow 생성이 가능하다") {
                // 실제 Kotlin Flow와 동일한 원리
                fun <T> customFlow(
                    block: suspend FlowCollector<T>.() -> Unit  // 핵심: 수신자 타입
                ): Flow<T> {
                    return object : Flow<T> {
                        override suspend fun collect(collector: FlowCollector<T>) {
                            collector.block()  // 수신자로 실행
                        }
                    }
                }
                
                runBlocking {
                    val collectedValues = mutableListOf<Int>()
                    
                    // 사용자 친화적인 Flow 생성
                    val myFlow = customFlow<Int> {
                        emit(1)     // this.emit(1)와 동일
                        emit(2)     // this.emit(2)와 동일  
                        emit(3)     // this.emit(3)와 동일
                        
                        // 여기서 this는 FlowCollector<Int> 타입
                        println("FlowCollector 컨텍스트에서 실행 중")
                    }
                    
                    myFlow.collect { value ->
                        collectedValues.add(value)
                        println("수집된 값: $value")
                    }
                    
                    collectedValues shouldBe listOf(1, 2, 3)
                }
            }
        }
        
        `when`("수신자 타입의 스코프 안전성 확인") {
            then("emit은 오직 FlowCollector 스코프에서만 호출 가능하다") {
                fun <T> demonstrateScopeFlow(
                    block: suspend FlowCollector<T>.() -> Unit
                ): Flow<T> {
                    return object : Flow<T> {
                        override suspend fun collect(collector: FlowCollector<T>) {
                            // 이 함수 스코프에서는 emit 호출 불가
                            // emit("test") // ❌ 컴파일 에러
                            
                            // collector를 수신자로 해야만 emit 가능
                            collector.block() // ✅ 가능
                        }
                    }
                }
                
                // 헬퍼 함수 - FlowCollector 스코프 밖
                suspend fun helperFunction() {
                    // 여기서는 emit 호출 불가
                    // emit("헬퍼에서") // ❌ 컴파일 에러
                    println("헬퍼 함수 실행")
                }
                
                runBlocking {
                    val result = mutableListOf<String>()
                    
                    val scopedFlow = demonstrateScopeFlow<String> {
                        // 이 스코프에서는 emit 호출 가능
                        emit("스코프 내부")  // ✅ 가능
                        emit("안전한 호출") // ✅ 가능
                        
                        // 다른 함수를 호출해도...
                        helperFunction()
                    }
                    
                    scopedFlow.collect { value ->
                        result.add(value)
                    }
                    
                    result shouldBe listOf("스코프 내부", "안전한 호출")
                }
            }
        }
    }
})

// 수신자 타입 확장 함수로 FlowCollector 스코프에서 호출되는 헬퍼
suspend fun FlowCollector<String>.emitGreeting(name: String) {
    emit("Hello, $name!")  // FlowCollector의 확장 함수이므로 emit 가능
}
